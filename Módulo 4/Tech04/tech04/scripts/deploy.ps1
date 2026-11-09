param(
 [Parameter(Mandatory)][string]$AllowedIp,
 [Parameter(Mandatory)][string]$Location,
 [string]$ResourceGroup = 'rg-tech04-academico',
 [switch]$Preview
)
$ErrorActionPreference = 'Stop'
if ($AllowedIp -notmatch '^\d{1,3}(\.\d{1,3}){3}/32$') { throw 'Informe somente seu IPv4 público seguido de /32.' }
$parsedIp = $null
if (-not [System.Net.IPAddress]::TryParse($AllowedIp.Split('/')[0], [ref]$parsedIp)) { throw 'IPv4 inválido.' }
$repoPath = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $repoPath
$az = (Get-Command az -ErrorAction SilentlyContinue).Source
if (-not $az) { $az = 'C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin\az.cmd' }
function Invoke-Azure {
 param([string[]]$Arguments)
 & $az @Arguments
 if ($LASTEXITCODE -ne 0) { throw 'Comando Azure falhou. Deploy interrompido.' }
}
Invoke-Azure -Arguments @('account','show','--query','{assinatura:name,status:state}','-o','table')
if ($Preview) {
 Write-Host 'O preview exige que o grupo de recursos já exista.'
 Invoke-Azure -Arguments @('deployment','group','what-if','-g',$ResourceGroup,'--template-file','infra/main.bicep','--parameters',"allowedIp=$AllowedIp","location=$Location")
 return
}
& mvn -B clean verify
if ($LASTEXITCODE -ne 0) { throw 'Build ou testes falharam.' }
Invoke-Azure -Arguments @('group','create','--name',$ResourceGroup,'--location',$Location,'--tags','purpose=academic','project=tech04','--output','none')
$app = Invoke-Azure -Arguments @('deployment','group','create','--name','tech04','-g',$ResourceGroup,'--template-file','infra/main.bicep','--parameters',"allowedIp=$AllowedIp","location=$Location",'--query','properties.outputs.functionAppName.value','-o','tsv')
$package = Join-Path $repoPath 'target/azure-functions/tech04-local'
$zip = Join-Path $repoPath 'target/functions.zip'
$packageItems = Get-ChildItem -LiteralPath $package | Where-Object { $_.Name -ne 'local.settings.json' }
Compress-Archive -LiteralPath $packageItems.FullName -DestinationPath $zip -Force
Invoke-Azure -Arguments @('functionapp','deployment','source','config-zip','-g',$ResourceGroup,'-n',"$app",'--src',$zip,'--output','none')
Write-Host "Deploy concluído: $app. O envio de e-mail ainda precisa ser configurado."
