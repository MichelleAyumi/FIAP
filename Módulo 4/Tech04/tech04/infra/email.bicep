param suffix string = uniqueString(resourceGroup().id)
resource email 'Microsoft.Communication/emailServices@2023-03-31' = {
 name: 'email-tech04-${suffix}'
 location: 'global'
 properties: { dataLocation: 'Europe' }
}
resource domain 'Microsoft.Communication/emailServices/domains@2023-03-31' = {
 parent: email
 name: 'AzureManagedDomain'
 location: 'global'
 properties: { domainManagement: 'AzureManaged', userEngagementTracking: 'Disabled' }
}
resource communication 'Microsoft.Communication/communicationServices@2023-03-31' = {
 name: 'acs-tech04-${suffix}'
 location: 'global'
 properties: { dataLocation: 'Europe', linkedDomains: [domain.id] }
}
output communicationServiceId string = communication.id
output domainId string = domain.id
