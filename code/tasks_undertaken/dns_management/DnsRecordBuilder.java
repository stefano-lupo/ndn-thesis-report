public interface DnsRecordBuilder<T> {
  default Multimap<String, RecordRequest> buildDkimRecords(T entity) {
    return ArrayListMultimap.create();
  }

  default Multimap<String, RecordRequest> buildARecords(T entity) {
    return ArrayListMultimap.create();
  }

  default Multimap<String, RecordRequest> buildMxRecords(T entity) {
    return ArrayListMultimap.create();
  }

  default Multimap<String, RecordRequest> buildSpfRecords(T entity) {
    return ArrayListMultimap.create();
  }

  default Map<String, String> buildPtrRecords(T entity) {
    return Collections.emptyMap();
  }

  boolean appliesTo(T entity);

  default Map<String, DnsConfiguration> getDnsConfigurationByZone(T entity) {
    Map<String, DnsConfiguration> dnsUpdatesByZone = new HashMap<>();
    Multimap<String, RecordRequest> allRecordRequestsByZone = ArrayListMultimap.create();

    allRecordRequestsByZone.putAll(buildDkimRecords(entity));
    allRecordRequestsByZone.putAll(buildARecords(entity));
    allRecordRequestsByZone.putAll(buildMxRecords(entity));
    allRecordRequestsByZone.putAll(buildSpfRecords(entity));

    for (String zone : allRecordRequestsByZone.keySet()) {
      dnsUpdatesByZone.put(zone, DnsConfiguration.builder()
          .setZone(zone)
          .setRecords(allRecordRequestsByZone.get(zone))
          .build());
    }

    return dnsUpdatesByZone;
  }
}