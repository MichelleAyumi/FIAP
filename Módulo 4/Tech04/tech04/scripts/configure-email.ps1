param(
 [Parameter(Mandatory)][string]$AdminEmail,
 [string]$ResourceGroup = 'rg-tech04-academico'
)
$ErrorActionPreference = 'Stop'
if ($AdminEmail -notmatch '^[^\s@]+@[^\s@]+\.[^\s@]+$') { throw 'E-mail inválido' }
$az = (Get-Command az -ErrorAction SilentlyContinue).Source
if (-not $az) { $az = 'C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin\az.cmd' }
function Invoke-Azure {
 param([string[]]$Arguments)
 $result = & $az @Arguments
 if ($LASTEXITCODE -ne 0) { throw 'Falha no comando Azure; configuração interrompida.' }
 return $result
}
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
Invoke-Azure -Arguments @('provider','register','--namespace','Microsoft.Communication','--wait','--output','none')
$emailDeployment = Invoke-Azure -Arguments @('deployment','group','create','-g',$ResourceGroup,'-n','tech04-email','--template-file','infra/email.bicep','--query','properties.outputs','-o','json') | ConvertFrom-Json
$app = Invoke-Azure -Arguments @('deployment','group','show','-g',$ResourceGroup,'-n','tech04','--query','properties.outputs.functionAppName.value','-o','tsv')
$domainId = $emailDeployment.domainId.value
$serviceId = $emailDeployment.communicationServiceId.value
$domain = Invoke-Azure -Arguments @('rest','--method','get','--url',"https://management.azure.com${domainId}?api-version=2023-03-31",'-o','json') | ConvertFrom-Json
$keys = Invoke-Azure -Arguments @('rest','--method','post','--url',"https://management.azure.com${serviceId}/listKeys?api-version=2023-03-31",'-o','json') | ConvertFrom-Json
if (-not $domain.properties.mailFromSenderDomain -or -not $keys.primaryConnectionString) { throw 'Domínio ou chave indisponível. Aguardar e repetir.' }
# Apenas em memória e arquivo temporário, nunca na linha de comando ou saída.
$settingsPath = Join-Path ([System.IO.Path]::GetTempPath()) ([guid]::NewGuid().ToString() + '.json')
try {
 @{
  EMAIL_CONNECTION_STRING = $keys.primaryConnectionString
  EMAIL_SENDER = "DoNotReply@$($domain.properties.mailFromSenderDomain)"
  ADMIN_EMAIL = $AdminEmail
  'AzureWebJobs.EnviarAlerta.Disabled' = 'false'
 } | ConvertTo-Json | Set-Content -LiteralPath $settingsPath
 Invoke-Azure -Arguments @('functionapp','config','appsettings','set','-g',$ResourceGroup,'-n',"$app",'--settings',"@$settingsPath",'--output','none')
} finally {
 Remove-Item -LiteralPath $settingsPath -Force -ErrorAction SilentlyContinue
 $keys = $null
}
Write-Host 'Serviço de e-mail configurado. Confirmar o envio e a entrega em teste separado.'
