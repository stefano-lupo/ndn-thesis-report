public boolean loginClient(LoginRequest loginRequest) {
  logLoginAttempt(loginRequest);
  Account account = accountsDao.getAccount(loginRequest.username);
  return account.password == loginRequest.password;
}