/**
 * Tries to find an IP that will not break up a contiguous block. This works by recursively finding the bucket with the fewest IPs.
 * This repeats until a single IP is found
 * It starts by splitting IPs into buckets using 2 ^ AWKWARD_IP_STARTING_NUM_HOST_BITS as the initial bucket size
 * If it fails to find one at this block size, it uses the IPs from the bucket with the fewest IPs in it,
 * reduces the block size and tries again, ultimately falling back to a random IP
 */
static String findSingleAwkwardIp(List<String> availableIps) {
  List<String> remainingIps = new ArrayList<>(availableIps);
  int numHostBits = AWKWARD_IP_STARTING_NUM_HOST_BITS;

  while (true) {
    Multimap<Integer, String> ipsByBucket = getIpsByBucket(remainingIps, getMask(numHostBits));

    // Find bucket with least number of IPs
    Collection<String> ipsInBucketWithFewestIps = ipsByBucket.asMap().values().stream()
        .sorted(Comparator.comparingInt(Collection::size))
        .findFirst().get();

    if (ipsInBucketWithFewestIps.size() == 1) {
      return ipsInBucketWithFewestIps.iterator().next();
    }

    numHostBits--;
    remainingIps.clear();
    remainingIps.addAll(ipsInBucketWithFewestIps);
  }
}