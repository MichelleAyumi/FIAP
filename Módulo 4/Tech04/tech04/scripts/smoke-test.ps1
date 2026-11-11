param([string]$ResourceGroup = 'rg-tech04-academico')
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'http-response.ps1')
$az = (Get-Command az -ErrorAction SilentlyContinue).Source
if (-not $az) { $az = 'C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin\az.cmd' }
function Invoke-Azure {
 param([string[]]$Arguments)
 $result = & $az @Arguments
 if ($LASTEXITCODE -ne 0) { throw 'Falha na consulta Azure.' }
 return $result
}
$outputs = Invoke-Azure -Arguments @('deployment','group','show','-g',$ResourceGroup,'-n','tech04','--query','properties.outputs','-o','json') | ConvertFrom-Json
$app = $outputs.functionAppName.value
$url = $outputs.endpoint.value
$body = @{ descricao = 'Teste acadêmico fictício - aula de cloud'; nota = 9 } | ConvertTo-Json
$withoutKey = Invoke-ProjectHttp -Uri $url -Body ([text.encoding]::UTF8.GetBytes($body))
if ($withoutKey.StatusCode -notin @(401, 403)) { throw "Esperado acesso negado sem chave; recebido $($withoutKey.StatusCode)." }
$key = Invoke-Azure -Arguments @('functionapp','keys','list','-g',$ResourceGroup,'-n',"$app",'--query','functionKeys.default','-o','tsv')
if (-not $key) { throw 'Chave de função indisponível.' }
try {
 $headers = @{ 'x-functions-key' = "$key" }
 $invalid = Invoke-ProjectHttp -Uri $url -Headers $headers -Body ([text.encoding]::UTF8.GetBytes('{"nota":2.5}'))
 if ($invalid.StatusCode -ne 400) { throw "Esperado 400 para dados inválidos; recebido $($invalid.StatusCode)." }
 $valid = Invoke-ProjectHttp -Uri $url -Headers $headers -Body ([text.encoding]::UTF8.GetBytes($body))
 if ($valid.StatusCode -ne 201) { throw "Esperado 201 para avaliação válida; recebido $($valid.StatusCode)." }
 Write-Host "Sem chave: $($withoutKey.StatusCode); inválida: $($invalid.StatusCode); válida: $($valid.StatusCode)."
 Write-Host "Resultado: $($valid.Content)"
} finally { $key = $null; $headers = $null }
# Nota 9 não gera alerta. Este script não envia e-mail.
