package sip.client;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            throw new RuntimeException("Please start sip client with specified your sip address, sip server address and protocol, example: java -jar client.jar login@127.0.0.1 127.0.0.1:1423 tcp");
        }
        String localIp = args[0].split("@")[1];
        String login = args[0].split("@")[0];
        String ip = args[1].split(":")[0];
        String port = args[1].split(":")[1];
        String protocol = args[2];

        SipClient client = new SipClient(localIp,login,true);
        client.startClient();
        client.connect(ip, Integer.parseInt(port), protocol);
    }
}
