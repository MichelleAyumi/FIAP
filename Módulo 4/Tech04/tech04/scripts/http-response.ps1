# Compatível com Windows PowerShell 5.1 e PowerShell 7.
function Invoke-ProjectHttp {
 param(
  [Parameter(Mandatory)][string]$Uri,
  [hashtable]$Headers = @{},
  [Parameter(Mandatory)][byte[]]$Body
 )
 Add-Type -AssemblyName System.Net.Http
 $client = [System.Net.Http.HttpClient]::new()
 $request = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post, $Uri)
 $result = $null
 try {
  $client.Timeout = [TimeSpan]::FromSeconds(60)
  foreach ($name in $Headers.Keys) { $request.Headers.Add($name, [string]$Headers[$name]) }
  $request.Content = [System.Net.Http.ByteArrayContent]::new($Body)
  $request.Content.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse('application/json; charset=utf-8')
  $result = $client.SendAsync($request).GetAwaiter().GetResult()
  # HTTP 400/401 são resultados esperados dos testes, não erros do script.
  [pscustomobject]@{
   StatusCode = [int]$result.StatusCode
   Content = $result.Content.ReadAsStringAsync().GetAwaiter().GetResult()
  }
 } finally {
  if ($null -ne $result) { $result.Dispose() }
  $request.Dispose()
  $client.Dispose()
 }
}
