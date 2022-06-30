package sip.security;

import sip.SipRequest;

import java.util.HashMap;

public class Security {
    private final HashMap<String, User> users = new HashMap<>();
    private final HashMap<String, String> userLoginByRemoteAddress = new HashMap<>();

    public Security() {
    }

    public boolean process(SipRequest request) {
        return this.hasUser(request.getRemoteIp(), request.getRemotePort());
    }

    public void addUser(User user) {
        this.users.put(user.getLogin(), user);
        this.userLoginByRemoteAddress.put(String.format("%s:%d", user.getRemoteIp(), user.getRemotePort()), user.getLogin());
        System.out.printf("Successfully registered new user %s at %s:%s \n", user.getLogin(), user.getHost(), user.getPort());
    }

    public void addUser(SipRequest request) {
        User user = new User();
        String[] hostAndPort = request.getContactAddress().split(":");
        user
                .setLogin(request.getContactName())
                .setHost(hostAndPort[0])
                .setPort(Integer.parseInt(hostAndPort[1]))
                .setSecret(Double.toString(Math.random() * 1000))
                .setRemoteIp(request.getRemoteIp())
                .setRemotePort(request.getRemotePort());

        this.addUser(user);
    }

    public void removeUser(SipRequest request) {
        removeUser(request.getContactName());
    }

    public void removeUser(User user) {
        removeUser(user.getLogin());
    }

    public void removeUser(String login) {
        User user;
        if ((user = this.users.remove(login)) != null) {
            this.userLoginByRemoteAddress.remove(String.format("%s:%d", user.getRemoteIp(), user.getRemotePort()));
            System.out.printf("Successfully unregistered user %s", login);
        }
    }

    public boolean hasUser(String login, String host, int port) {
        return this.hasUser(login);
    }

    public boolean hasUser(String ip, int port){
        return this.getUserByAddress(ip,port) != null;
    }

    public boolean hasUser(String login) {
        User user;
        return (user = users.get(login)) != null && userLoginByRemoteAddress.get(String.format("%s:%d", user.getRemoteIp(), user.getRemotePort())) != null;
    }

    public User getUser(String login) {
        return users.get(login);
    }

    public User getUser(SipRequest request) {
        return getUserByAddress(request.getRemoteIp(),request.getRemotePort());
    }

    public User getUserByAddress(String ip, int port) {
        return users.get(userLoginByRemoteAddress.get(String.format("%s:%d", ip, port)));
    }

    public HashMap<String, User> getUsers() {
        return this.users;
    }
}
