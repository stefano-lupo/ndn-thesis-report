public class IpAddressUtils {
  private static final Logger LOG = LoggerFactory.getLogger(IpAddressUtils.class);
  private static final int AWKWARD_IP_STARTING_NUM_HOST_BITS = 3;

  public static List<String> getContiguousIpsWithExistingIps(List<String> availableIps, List<String> requiredIps, int totalNumIps) {
    return requiredIps.size() == 0 ? getContiguousIps(availableIps, totalNumIps)
        : doGetContiguousIpsWithExistingIps(availableIps, requiredIps, totalNumIps, findStartingNumberOfHostBits(requiredIps, totalNumIps));
  }

  private static List<String> doGetContiguousIpsWithExistingIps(List<String> availableIps, List<String> existingIps, int totalNumIps, int numHostBits) {
    if (numHostBits == 0) {
      List<String> bestIps = new ArrayList<>(existingIps);
      bestIps.addAll(getContiguousIps(availableIps, totalNumIps - existingIps.size()));
      return bestIps;
    }

    int maxBucketSize = getNumberOfHosts(numHostBits);
    int mask = getMask(numHostBits);

    Multimap<Integer, String> requiredIpsByBucket = getIpsByBucket(existingIps, mask);
    Multimap<Integer, String> availableIpsByBucket = getIpsByBucket(availableIps, mask);

    List<String> ipsToUse = new ArrayList<>();
    Set<String> requiredIpsRemaining = new HashSet<>(existingIps);

    for (Entry<Integer, Collection<String>> entry : requiredIpsByBucket.asMap().entrySet()) {
      int bucket = entry.getKey();
      List<String> requiredIpsInBucket = new ArrayList<>(entry.getValue());

      Collection<String> availableIpsInBucket = availableIpsByBucket.containsKey(bucket) ?
          availableIpsByBucket.get(bucket) :
          Collections.emptyList();

      int requiredNumIpsToFillBucket = maxBucketSize - requiredIpsInBucket.size();

      // If the existing ips will fit into this bucket with the new ips
      if (availableIpsInBucket.size() == requiredNumIpsToFillBucket
          && totalNumIps >= ipsToUse.size() + maxBucketSize) {
        ipsToUse.addAll(availableIpsInBucket);
        ipsToUse.addAll(requiredIpsInBucket);
        requiredIpsRemaining.removeAll(requiredIpsInBucket);
        if (ipsToUse.size() == totalNumIps) {
          return ipsToUse;
        }
      }
    }

    // Ensure no duplicate ips will be used in subsequent searches
    List<String> availableIpsForNextSearch = new ArrayList<>(availableIps);
    availableIpsForNextSearch.removeAll(ipsToUse);

    List<String> finalIps = new ArrayList<>(ipsToUse);
    int numIpsRemaining = totalNumIps - ipsToUse.size();

    // All existing ips have been used, get the best IPs we can for the rest
    if (requiredIpsRemaining.isEmpty()) {
      finalIps.addAll(getContiguousIps(availableIpsForNextSearch, numIpsRemaining));
      return finalIps;
    }

    finalIps.addAll(doGetContiguousIpsWithExistingIps(availableIpsForNextSearch, new ArrayList<>(requiredIpsRemaining), numIpsRemaining, numHostBits - 1));
    return finalIps;
  }

  public static List<String> getContiguousIps(List<String> availableIps, int totalNumIps) {
    if (totalNumIps == 1) {
      return Collections.singletonList(findSingleAwkwardIp(availableIps));
    }

    int numHostBits = getMaxNumberOfHostBits(totalNumIps);
    int maxBucketSize = getNumberOfHosts(numHostBits);

    // Find number of ips that would overflow this block
    int extraIpsNeeded = totalNumIps - maxBucketSize;
    int mask = getMask(numHostBits);

    // Try find a contiguous block
    Multimap<Integer, String> ipsByBucket = getIpsByBucket(availableIps, mask);
    int ipsNeededThisBlockSize = totalNumIps - extraIpsNeeded;
    List<String> bestIps = new ArrayList<>();
    for (Entry<Integer, Collection<String>> entry : ipsByBucket.asMap().entrySet()) {
      if (entry.getValue().size() == ipsNeededThisBlockSize) {
        bestIps.addAll(entry.getValue());
        break;
      }
    }

    // Ensure we don't find same ips at next call
    List<String> availableIpsForNextSearch = new ArrayList<>(availableIps);
    availableIpsForNextSearch.removeAll(bestIps);

    if (bestIps.isEmpty()) {
      LOG.debug("Couldn't find a contiguous block of size {}, trying smaller block size.", maxBucketSize);
      int numIpsForFirstSearch = getNumberOfHosts(numHostBits - 1);
      int numIpsForSecondSearch = totalNumIps - numIpsForFirstSearch;

      List<String> bestIpsOne = getContiguousIps(availableIpsForNextSearch, numIpsForFirstSearch);
      bestIps.addAll(bestIpsOne);

      if (numIpsForSecondSearch > 0) {
        availableIpsForNextSearch.removeAll(bestIpsOne);
        List<String> bestIpsTwo = getContiguousIps(availableIpsForNextSearch, numIpsForSecondSearch);
        bestIps.addAll(bestIpsTwo);
      }
    } else if (extraIpsNeeded != 0) {
      LOG.debug("{} Extra IPs were required after block size {}, trying next block size", maxBucketSize);
      bestIps.addAll(getContiguousIps(availableIpsForNextSearch, extraIpsNeeded));
    }

    return bestIps;
  }

  static String findSingleAwkwardIp(List<String> availableIps) {
    List<String> remainingIps = new ArrayList<>(availableIps);
    int numHostBits = AWKWARD_IP_STARTING_NUM_HOST_BITS;

    while (true) {
      Multimap<Integer, String> ipsByBucket = getIpsByBucket(remainingIps, getMask(numHostBits));

      // Find bucket with least number of IPs
      Collection<String> ipsInBucketWithFewestIps = ipsByBucket.asMap().values().stream()
          .sorted((x, y) -> x.size() > y.size() ? 1 : -1)
          .findFirst().get();

      if (ipsInBucketWithFewestIps.size() == 1) {
        return ipsInBucketWithFewestIps.iterator().next();
      }

      numHostBits--;
      remainingIps.clear();
      remainingIps.addAll(ipsInBucketWithFewestIps);
    }
  }

  @VisibleForTesting
  static int findStartingNumberOfHostBits(List<String> requiredIps, int totalIps) {
    int numHostBits = getMaxNumberOfHostBits(totalIps);

    while (true) {
      int maxBucketSize = getNumberOfHosts(numHostBits);
      int mask = getMask(numHostBits);
      int newIpsToCreate = totalIps - requiredIps.size();

      Multimap<Integer, String> currentIpsByBucket = getIpsByBucket(requiredIps, mask);

      // Ensure that at least one of the potential buckets could be 
      // filled by creating newIpsToCreate IPs
      // If we're not adding enough IPs to potentially 
      // fill the bucket, theres no point
      if (currentIpsByBucket.asMap().values().stream().anyMatch(ips -> maxBucketSize - ips.size() <= newIpsToCreate)) {
        LOG.debug("Starting with bucket size of {}", numHostBits);
        return numHostBits;
      } else {
        LOG.debug("No point in checking a bucket size of {}", numHostBits);
        numHostBits--;
      }
    }
  }

  private static int getMaxNumberOfHostBits(int numIps) {
    int tailBits = 0;
    int toShift = numIps;
    while (toShift >= 2) {
      tailBits ++;
      toShift = toShift >> 1;
    }
    return tailBits;
  }

  private static int getNumberOfHosts(int numHostBits) {
    return (int) Math.pow(2, numHostBits);
  }

  private static int getMask(int numHostBits) {
    return toBigEndian(0xffffffff << numHostBits);
  }

  private static Multimap<Integer, String> getIpsByBucket(List<String> ips, int mask) {
    Multimap<Integer, String> ipsByBucket = ArrayListMultimap.create();
    for (String ipString : ips) {
      int ip = getIntFromIp(ipString);
      int bucket = ip & mask;
      ipsByBucket.put(bucket, ipString);
    }

    return ipsByBucket;
  }

  private static int getIntFromIp(String ip) {
    int value = 0;
    String[] pieces = ip.split("\\.");
    for (int i = 0; i < pieces.length; i++) {
      int piece = Integer.valueOf(pieces[i]);
      value += piece << i * 8;
    }

    return value;
  }

  private static int toBigEndian(int littleEndian) {
    byte[] b = ByteBuffer.allocate(4).putInt(littleEndian).array();
    return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
  }
}