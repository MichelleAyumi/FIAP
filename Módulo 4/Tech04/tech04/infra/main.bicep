param location string = resourceGroup().location
param suffix string = uniqueString(resourceGroup().id)
@description('IP público autorizado, no formato IPv4/32. Não usar 0.0.0.0/0.')
param allowedIp string

var storageName = 'sttech04${suffix}'
resource storage 'Microsoft.Storage/storageAccounts@2023-05-01' = {
 name: storageName
 location: location
 sku: { name: 'Standard_LRS' }
 kind: 'StorageV2'
 properties: {
  minimumTlsVersion: 'TLS1_2'
  supportsHttpsTrafficOnly: true
  allowBlobPublicAccess: false
 }
}
resource blobs 'Microsoft.Storage/storageAccounts/blobServices@2023-05-01' = {
 parent: storage
 name: 'default'
 properties: {}
}
resource reports 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-05-01' = {
 parent: blobs
 name: 'relatorios'
 properties: { publicAccess: 'None' }
}
resource packages 'Microsoft.Storage/storageAccounts/blobServices/containers@2023-05-01' = {
 parent: blobs
 name: 'deployments'
 properties: { publicAccess: 'None' }
}
resource tables 'Microsoft.Storage/storageAccounts/tableServices@2023-05-01' = {
 parent: storage
 name: 'default'
 properties: {}
}
resource evaluations 'Microsoft.Storage/storageAccounts/tableServices/tables@2023-05-01' = {
 parent: tables
 name: 'avaliacoes'
 properties: {}
}
resource queues 'Microsoft.Storage/storageAccounts/queueServices@2023-05-01' = {
 parent: storage
 name: 'default'
 properties: {}
}
resource alerts 'Microsoft.Storage/storageAccounts/queueServices/queues@2023-05-01' = {
 parent: queues
 name: 'alertas-criticos'
 properties: {}
}
resource workspace 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
 name: 'log-tech04-${suffix}'
 location: location
 properties: { sku: { name: 'PerGB2018' }, retentionInDays: 30 }
}
resource insights 'Microsoft.Insights/components@2020-02-02' = {
 name: 'appi-tech04-${suffix}'
 location: location
 kind: 'web'
 properties: { Application_Type: 'web', WorkspaceResourceId: workspace.id }
}
resource plan 'Microsoft.Web/serverfarms@2024-04-01' = {
 name: 'plan-tech04-${suffix}'
 location: location
 kind: 'functionapp'
 sku: { tier: 'FlexConsumption', name: 'FC1' }
 properties: { reserved: true }
}
resource app 'Microsoft.Web/sites@2024-04-01' = {
 name: 'func-tech04-${suffix}'
 location: location
 kind: 'functionapp,linux'
 identity: { type: 'SystemAssigned' }
 properties: {
  serverFarmId: plan.id
  httpsOnly: true
  publicNetworkAccess: 'Enabled'
  functionAppConfig: {
   deployment: {
    storage: {
     type: 'blobContainer'
     value: '${storage.properties.primaryEndpoints.blob}deployments'
     authentication: { type: 'SystemAssignedIdentity' }
    }
   }
   runtime: { name: 'java', version: '21' }
   scaleAndConcurrency: { maximumInstanceCount: 40, instanceMemoryMB: 512 }
  }
  siteConfig: {
   minTlsVersion: '1.2'
   ftpsState: 'Disabled'
   ipSecurityRestrictionsDefaultAction: 'Deny'
   ipSecurityRestrictions: [
    { name: 'Computador autorizado', ipAddress: allowedIp, action: 'Allow', priority: 100 }
   ]
   scmIpSecurityRestrictionsUseMain: true
   appSettings: [
    { name: 'AzureWebJobsStorage__accountName', value: storage.name }
    { name: 'AzureWebJobsStorage__credential', value: 'managedidentity' }
    { name: 'DATA_STORAGE_ACCOUNT', value: storage.name }
    { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: insights.properties.ConnectionString }
    { name: 'WEEKLY_SCHEDULE', value: '0 0 12 * * 1' }
    // Só habilitar após configurar o serviço de e-mail para preservar a fila.
    { name: 'AzureWebJobs.EnviarAlerta.Disabled', value: 'true' }
   ]
  }
 }
}
var roles = [
 'b7e6dc6d-f1e8-4753-8033-0f276bb0955b' // Storage Blob Data Owner (requisito do host Functions)
 '974c5e8b-45b9-4653-ba55-5f855dd0fb88' // Storage Queue Data Contributor
 '0a9a7e1f-b9d0-4cc4-a60d-0319b160aaa3' // Storage Table Data Contributor
]
resource assignments 'Microsoft.Authorization/roleAssignments@2022-04-01' = [for role in roles: {
 name: guid(storage.id, app.id, role)
 scope: storage
 properties: {
  roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', role)
  principalId: app.identity.principalId
  principalType: 'ServicePrincipal'
 }
}]
output functionAppName string = app.name
output storageAccountName string = storage.name
output applicationInsightsName string = insights.name
output endpoint string = 'https://${app.properties.defaultHostName}/api/avaliacao'
