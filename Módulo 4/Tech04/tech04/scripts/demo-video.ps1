param(
 [Parameter(Mandatory)][ValidateSet('SemChave','Invalida','Normal','Alerta','Relatorio')][string]$Step,
 [switch]$SendEmail,
 [string]$ResourceGroup = 'rg-tech04-academico'
)
$ErrorActionPreference = 'Stop'
if ($Step -eq 'Alerta' -and -not $SendEmail) { throw 'Este passo envia um e-mail real. Use -SendEmail somente quando quiser efetuar o envio.' }
$az = (Get-Command az -ErrorAction SilentlyContinue).Source
if (-not $az) { $az = 'C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin\az.cmd' }
function Invoke-Azure {
 param([string[]]$Arguments)
 $result = & $az @Arguments
 if ($LASTEXITCODE -ne 0) { throw 'Consulta Azure falhou. Verifique o login.' }
 return $result
}
$outputs = Invoke-Azure -Arguments @('deployment','group','show','-g',$ResourceGroup,'-n','tech04','--query','properties.outputs','-o','json') | ConvertFrom-Json
$app = $outputs.functionAppName.value
$url = $outputs.endpoint.value
$key = $null
try {
 if ($Step -eq 'Relatorio') {
  $key = Invoke-Azure -Arguments @('functionapp','keys','list','-g',$ResourceGroup,'-n',"$app",'--query','masterKey','-o','tsv')
  $response = Invoke-WebRequest -Uri "https://$app.azurewebsites.net/admin/functions/GerarRelatorioSemanal" -Method Post -Headers @{ 'x-functions-key'="$key" } -ContentType 'application/json' -Body '{"input":""}' -TimeoutSec 60
  Write-Host "HTTP $($response.StatusCode) - execução manual aceita. Conferir o blob e os logs; 202 não confirma conclusão."
  return
 }
 $headers = @{}
 if ($Step -ne 'SemChave') {
  $key = Invoke-Azure -Arguments @('functionapp','keys','list','-g',$ResourceGroup,'-n',"$app",'--query','functionKeys.default','-o','tsv')
  if (-not $key) { throw 'Chave da função indisponível.' }
  $headers['x-functions-key']="$key"
 }
 $body = switch ($Step) {
  'Invalida' { '{"descricao":"Teste fictício de validação","nota":11}' }
  'Alerta' { '{"descricao":"TESTE ACADEMICO - alerta fictício durante a demonstração.","nota":2}' }
  default { '{"descricao":"TESTE ACADEMICO - aula de cloud bem explicada.","nota":9}' }
 }
 Write-Host "POST $url"
 Write-Host "Corpo: $body"
 $response = Invoke-WebRequest -Uri $url -Method Post -Headers $headers -ContentType 'application/json; charset=utf-8' -Body ([text.encoding]::UTF8.GetBytes($body)) -SkipHttpErrorCheck -TimeoutSec 60
 Write-Host "HTTP $($response.StatusCode)"
 Write-Host $response.Content
} finally { $key=$null; $headers=$null }
