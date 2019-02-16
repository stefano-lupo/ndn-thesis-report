public boolean immutableLoginCLient(LoginRequest loginRequest) {
  LoginRequest loginRequestCopy = new LoginRequest(
    loginRequest.username,
    loginRequest.password,
    loginRequest.ip);
  logLoginAttempt(loginRequestCopy);
  Account account = accountsDao.getAccount(loginRequest.username);
  return account.password == loginRequest.password;
}