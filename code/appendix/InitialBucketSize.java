/**
 * Find the theoretical best case bucket size given a list of existing ips and a total number of ips
 * Eg [54.174.58.0, 54.174.58.100] and a total size of 4:
 * There is no point in starting with a bucket size of 4 as even if we had all the IPs in the world,
 * we cant create a /30 block with those two existing IPs, so start with /31
 */
static int findStartingNumberOfHostBits(List<String> requiredIps, int totalIps) {
  int numHostBits = getMaxNumberOfHostBits(totalIps);

  while (true) {
    int maxBucketSize = getNumberOfHosts(numHostBits);
    int mask = getMask(numHostBits);
    int newIpsToCreate = totalIps - requiredIps.size();

    Multimap<Integer, String> currentIpsByBucket = getIpsByBucket(requiredIps, mask);

    // Ensure that at least one of the potential buckets could be filled by creating newIpsToCreate IPs
    // If we're not adding enough IPs to potentially fill the bucket, theres no point
    if (currentIpsByBucket.asMap().values().stream().anyMatch(ips -> maxBucketSize - ips.size() <= newIpsToCreate)) {
      LOG.debug("Starting with bucket size of {}", numHostBits);
      return numHostBits;
    } else {
      LOG.debug("No point in checking a bucket size of {}", numHostBits);
      numHostBits--;
    }
  }
}