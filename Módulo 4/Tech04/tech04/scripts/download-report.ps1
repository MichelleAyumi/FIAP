param([string]$ResourceGroup = 'rg-tech04-academico')
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
$az = (Get-Command az -ErrorAction SilentlyContinue).Source
if (-not $az) { $az = 'C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin\az.cmd' }
function Invoke-Azure {
 param([string[]]$Arguments)
 $result = & $az @Arguments
 if ($LASTEXITCODE -ne 0) { throw 'Comando Azure falhou; download interrompido.' }
 return $result
}
$account = Invoke-Azure -Arguments @('deployment','group','show','-g',$ResourceGroup,'-n','tech04','--query','properties.outputs.storageAccountName.value','-o','tsv')
$key = Invoke-Azure -Arguments @('storage','account','keys','list','-g',$ResourceGroup,'-n',"$account",'--query','[0].value','-o','tsv')
try {
 $blobs = Invoke-Azure -Arguments @('storage','blob','list','--account-name',"$account",'--account-key',"$key",'--container-name','relatorios','--output','json') | ConvertFrom-Json
 $latest = $blobs | Where-Object { $_.name -like 'semana-*.json' } | Sort-Object { [datetime]$_.properties.lastModified } -Descending | Select-Object -First 1
 if (-not $latest) { throw 'Nenhum relatório disponível. Execute a função e aguarde a conclusão.' }
 New-Item -ItemType Directory -Force output/data | Out-Null
 Invoke-Azure -Arguments @('storage','blob','download','--account-name',"$account",'--account-key',"$key",'--container-name','relatorios','--name',"$($latest.name)",'--file','output/data/relatorio-semanal.json','--overwrite','--output','none','--no-progress')
 $report = Get-Content -LiteralPath output/data/relatorio-semanal.json -Raw | ConvertFrom-Json
 if ($null -eq $report.quantidade -or -not $report.fusoHorario) { throw 'JSON do relatório inválido.' }
 @{ blob=$latest.name; storageAccount="$account"; lastModified=$latest.properties.lastModified; downloadedAt=[datetime]::UtcNow.ToString('o') } | ConvertTo-Json | Set-Content output/data/origem-relatorio.json
 Write-Host "Origem: $account / relatorios / $($latest.name)"
 Write-Host "Quantidade: $($report.quantidade); média: $($report.mediaNotas); fuso: $($report.fusoHorario)"
 Write-Host 'Arquivo: output/data/relatorio-semanal.json'
} finally { $key=$null }
