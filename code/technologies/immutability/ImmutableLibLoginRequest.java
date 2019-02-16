@Value.Immutable
interface LoginRequestIf {
  String getUsername();
  String getPassword();
  String getIp();
}