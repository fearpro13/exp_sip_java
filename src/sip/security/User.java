package sip.security;

public class User {
    private String login;
    private String host;
    private int port;
    private String secret;
    private String remoteIp;
    private int remotePort;

    public User() {

    }

    public String getLogin() {
        return login;
    }

    public User setLogin(String login) {
        this.login = login;

        return this;
    }

    public String getHost() {
        return host;
    }

    public User setHost(String host) {
        this.host = host;

        return this;
    }

    public int getPort() {
        return port;
    }

    public User setPort(int port) {
        this.port = port;

        return this;
    }

    public String getSecret() {
        return secret;
    }

    public User setSecret(String secret) {
        this.secret = secret;

        return this;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public User setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;

        return this;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public User setRemotePort(int remotePort) {
        this.remotePort = remotePort;

        return this;
    }
}
