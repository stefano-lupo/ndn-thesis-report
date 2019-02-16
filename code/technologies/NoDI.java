public class PizzaOrderingService {
  private SqlTransactionLogger transactionLogger;
  private AccountsDao AccountsDao;
  private PayPalCustomerBiller customerBiller;

  public PizzaOrderingService() {
    SqlCredentials sqlCredentials = new SqlCredentials("dbName", "password");
    SqlConnection sqlConnection = new SqlConnection(sqlCredentials);
    PayPalCredentials payPalCredentials = new PayPalCredentials("accountId", "password");
    this.transactionLogger = new SqlTransactionLogger(sqlConnection);
    this.accountsDao = new AccountsDao(sqlConnection); 
    this.customerBiller = new PayPalCustomerBiller(payPalCredentials);
  }
}