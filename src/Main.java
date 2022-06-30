import sip.server.SipServer;

public class Main {

    public static void main(String[] args){
        SipServer server = new SipServer();
        try {
            if(args.length < 3){
                throw new RuntimeException("Please specify server ip address, port and protocol, example: java -jar server ip port protocol");
            }

            String ip = args[0];
            String port = args[1];
            String protocol = args[2];
            if(ip == null || port == null || protocol == null){
                throw new RuntimeException("Please specify server ip address, port and protocol, example: java -jar server ip port protocol");
            }
            server.start(ip, Integer.parseInt(port),protocol);
        }catch (Throwable throwable){
            System.out.println("Server shut down\r\n");
            System.out.println(throwable.getMessage());
            server.interrupt();
        }
    }

}
