public <T> List<DnsConfiguration> buildHubSpotDnsRecordUpdatesForZones(T entity) {
  List<DnsConfiguration> hubSpotDnsChanges = new ArrayList<>();
  Set<DnsRecordBuilder<T>> buildersToApply = getDnsRecordBuildersToApplyOrThrow(entity);

  for (DnsRecordBuilder<T> dnsRecordBuilder : buildersToApply) {
    Map<String, DnsConfiguration> requiredConfigByZone = dnsRecordBuilder.getDnsConfigurationByZone(entity);
    for (Entry<String, DnsConfiguration> entry : requiredConfigByZone.entrySet()) {
      String zone = entry.getKey();
      DnsConfiguration config = entry.getValue();
      List<RecordRequest> changedHubSpotDomainRecords = dnsClientHelper.findRecordsToUpdate(config.getRecords(), zone);
      hubSpotDnsChanges.add(config.withRecords(changedHubSpotDomainRecords));
    }
  }

  return hubSpotDnsChanges;
}

private <T> Set<DnsRecordBuilder<T>> getDnsRecordBuildersToApplyOrThrow(T entity) {
  if (!recordBuildersByClass.contains(entity.getClass())) {
    throw new IllegalArgumentException(String.format("Invalid type given: %s", entity.getClass()));
  }
  
  return recordBuildersByClass.get(entity.getClass());
}