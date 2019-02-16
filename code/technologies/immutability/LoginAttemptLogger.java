class LoginAttemptLogger {
  LoginRequestDao loginRequestDao;

  @Inject
  public LoginAttemptLogger(LoginRequestDao loginRequestDao) {
    this.loginRequestDao = loginRequestDao;
  }

  public void logLoginAttempt(LoginRequest loginRequest) {
    // Don't log the users password to the database
    loginRequest.setPassword("");
    loginRequestDao.writeToTable(loginRequest);
  }
}