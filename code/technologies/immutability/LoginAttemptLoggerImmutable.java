public void logLoginAttempt(LoginRequest loginRequest) {
  // Don't log the users password to the database
  loginRequestDao.writeToTable(loginRequest.withPassword(""));
}