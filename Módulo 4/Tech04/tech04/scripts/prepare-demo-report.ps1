param([string]$ResourceGroup = 'rg-tech04-academico')
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
$az = (Get-Command az -ErrorAction SilentlyContinue).Source
if (-not $az) { $az = 'C:\Program Files\Microsoft SDKs\Azure\CLI2\wbin\az.cmd' }
function Invoke-Azure {
 param([string[]]$Arguments)
 $result = & $az @Arguments
 if ($LASTEXITCODE -ne 0) { throw 'Comando Azure falhou; preparação interrompida.' }
 return $result
}
$outputs = Invoke-Azure -Arguments @('deployment','group','show','-g',$ResourceGroup,'-n','tech04','--query','properties.outputs','-o','json') | ConvertFrom-Json
$account = $outputs.storageAccountName.value
$app = $outputs.functionAppName.value
$zone = [TimeZoneInfo]::FindSystemTimeZoneById('E. South America Standard Time')
$localNow = [TimeZoneInfo]::ConvertTimeFromUtc([datetime]::UtcNow, $zone)
$daysSinceMonday = (([int]$localNow.DayOfWeek + 6) % 7)
$weekStart = $localNow.Date.AddDays(-$daysSinceMonday - 7)
$seed = @(
 @{ day=0; hour=9;  grade=2;  text='DEMO - Explicação de deploy precisa de mais exemplos.' },
 @{ day=0; hour=14; grade=8;  text='DEMO - Exemplos de armazenamento claros.' },
 @{ day=1; hour=10; grade=3;  text='DEMO - Dificuldade para configurar permissões.' },
 @{ day=2; hour=9;  grade=7;  text='DEMO - Aula de funções bem organizada.' },
 @{ day=2; hour=11; grade=5;  text='DEMO - Ritmo da explicação poderia ser mais lento.' },
 @{ day=2; hour=15; grade=10; text='DEMO - Prática de monitoramento excelente.' },
 @{ day=3; hour=9;  grade=6;  text='DEMO - Reforçar o conceito de filas.' },
 @{ day=3; hour=14; grade=9;  text='DEMO - Demonstração prática ajudou no aprendizado.' },
 @{ day=4; hour=10; grade=1;  text='DEMO - Não consegui concluir o exercício de acesso.' },
 @{ day=4; hour=15; grade=8;  text='DEMO - Material complementar foi útil.' },
 @{ day=5; hour=9;  grade=4;  text='DEMO - Faltaram exemplos de configuração.' },
 @{ day=5; hour=14; grade=9;  text='DEMO - Revisão final esclareceu as dúvidas.' }
)
$key = Invoke-Azure -Arguments @('storage','account','keys','list','-g',$ResourceGroup,'-n',"$account",'--query','[0].value','-o','tsv')
$records = @()
try {
 for ($i = 0; $i -lt $seed.Count; $i++) {
  $s = $seed[$i]
  $localDate = [datetime]::SpecifyKind($weekStart.AddDays($s.day).AddHours($s.hour), [DateTimeKind]::Unspecified)
  $utc = [TimeZoneInfo]::ConvertTimeToUtc($localDate, $zone)
  $partition = $utc.ToString('yyyy-MM-dd')
  $row = 'demo-' + $weekStart.ToString('yyyyMMdd') + '-' + ($i + 1).ToString('00')
  $urgency = if ($s.grade -le 3) { 'CRITICA' } elseif ($s.grade -le 6) { 'ATENCAO' } else { 'NORMAL' }
  $entity = @{
   PartitionKey=$partition; RowKey=$row; descricao=$s.text
   nota=[int]$s.grade; 'nota@odata.type'='Edm.Int32'; urgencia=$urgency
   dataEnvio=$utc.ToString("yyyy-MM-dd'T'HH:mm:ss'Z'")
  }
  $path = "/avaliacoes(PartitionKey='$partition',RowKey='$row')"
  $date = [datetime]::UtcNow.ToString('R', [cultureinfo]::InvariantCulture)
  $hmac = [System.Security.Cryptography.HMACSHA256]::new([Convert]::FromBase64String("$key"))
  try { $signature = [Convert]::ToBase64String($hmac.ComputeHash([text.encoding]::UTF8.GetBytes("$date`n/$account$path"))) }
  finally { $hmac.Dispose() }
  $headers = @{
   'x-ms-date'=$date; 'x-ms-version'='2019-02-02'; 'DataServiceVersion'='3.0'
   'MaxDataServiceVersion'='3.0'; 'Accept'='application/json;odata=nometadata'
   'Authorization'="SharedKeyLite ${account}:$signature"
  }
  $null = Invoke-WebRequest -Uri "https://$account.table.core.windows.net$path" -Method Put -Headers $headers -ContentType 'application/json' -Body ([text.encoding]::UTF8.GetBytes(($entity | ConvertTo-Json))) -TimeoutSec 30
  $records += @{ id=$row; descricao=$s.text; nota=[int]$s.grade; urgencia=$urgency; dataEnvio=$entity.dataEnvio }
 }
 New-Item -ItemType Directory -Force output/data | Out-Null
 $records | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath output/data/avaliacoes-demonstracao.json -Encoding utf8
 $masterKey = Invoke-Azure -Arguments @('functionapp','keys','list','-g',$ResourceGroup,'-n',"$app",'--query','masterKey','-o','tsv')
 try {
  $null = Invoke-WebRequest -Uri "https://$app.azurewebsites.net/admin/functions/GerarRelatorioSemanal" -Method Post -Headers @{ 'x-functions-key'="$masterKey" } -ContentType 'application/json' -Body '{"input":""}' -TimeoutSec 60
 } finally { $masterKey=$null }
 Write-Host "12 registros fictícios preparados. Semana: $($weekStart.ToString('dd/MM/yyyy')) a $($weekStart.AddDays(6).ToString('dd/MM/yyyy'))."
 Write-Host 'Função semanal acionada manualmente. Nenhuma mensagem de e-mail foi solicitada por esta preparação.'
 Write-Host 'IDs fixos por semana: repetir atualiza os mesmos registros, sem duplicar a amostra.'
} finally { $key=$null; $headers=$null; $signature=$null }
