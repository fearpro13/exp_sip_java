package sipclientpool;

public class Main {
    public static void main(String[] args){
        SipClientPool pool = new SipClientPool();
        try {
            if(args.length < 4){
                throw new RuntimeException("Please specify local ip, server ip and port, server control port and protocol, example: 127.0.0.1 10.0.43.19:5505 9876 tcp ");
            }

            try {
                String localIp = args[0];
                String serverIp = args[1].split(":")[0];
                int serverPort = Integer.parseInt(args[1].split(":")[1]);
                int serverControlPort = Integer.parseInt(args[2]);
                String protocol = args[3];

                pool.startPool(localIp,serverIp,serverPort,serverControlPort,protocol);
            }catch (Throwable throwable){
                System.out.println("Please specify server ip address, port and protocol, example: java -jar server ip port protocol");
            }
        }catch (Throwable throwable){
            System.out.println("Client pool shutdown\r\n");
            throwable.printStackTrace();
        }
    }
}
