public class PizzaOrderingService {
  private SqlTransactionLogger transactionLogger;
  private PayPalCustomerBiller customerBiller;
  private AccountsDao accountsDao;

  @Inject
  public PizzaOrderingService(SqlTransactionLogger transactionLogger, 
                              PayPalCustomerBiller customerBiller,
                              AccountsDao accountsDao) {
    this.transactionLogger = transactionLogger;
    this.customerBiller = customerBiller;
    this.accountsDao = accountsDao;
  }
}