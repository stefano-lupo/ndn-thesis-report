public class DnsSyncJob extends AbstractJob {
  private final AccountsDao accountsDao;
  private final DnsSyncJobHelper dnsSyncJobHelper;
  private final DnsClientHelper dnsClientHelper;

  @Inject
  public DnsSyncJob(AccountsDao accountsDao,
                    DnsSyncJobHelper dnsSyncJobHelper,
                    DnsClientHelper dnsClientHelper) {
    this.accountsDao = accountsDao;
    this.dnsSyncJobHelper = dnsSyncJobHelper;
    this.dnsClientHelper = dnsClientHelper;
  }

  @Override
  public void run() {
    Collection<Account> accountsToSync = accountsDao.getAllAccounts();
    syncDnsForEntities(accountsToSync);

    // Similar code for Ip addresses and other entities..
  }

  private <T> void syncDnsForEntities(Collection<T> entities) {
    for (T entity : entities) {
      checkEntity(entity);
    }
  }

  private <T> void checkEntity(T entity) {
    List<DnsConfiguration> dnsConfigurationsForZones = dnsSyncJobHelper.buildDnsRecordUpdatesForZones(entity);
    for (DnsConfiguration dnsConfiguration : dnsConfigurationsForZones) {
      for (RecordRequest recordRequest : dnsConfiguration.getRecords()) {
        dnsClientHelper.upsertRecord(recordRequest, accountDnsConfiguration.getZone());
      }
    }
  }
}