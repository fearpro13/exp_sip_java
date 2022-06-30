package udp;

import server.AbstractServer;
import server.NetworkMessageReceiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpServer extends AbstractServer {
    private final byte[] receiveBuffer = new byte[2048];

    private final NetworkMessageReceiver receiver;

    private DatagramSocket datagramSocket;

    public UdpServer(String ip, int port, NetworkMessageReceiver receiver){
        this.ip = ip;
        this.port = port;
        this.protocol = "UDP";

        this.receiver = receiver;
    }

    @Override
    public void startServer(){
        try {
            this.datagramSocket = new DatagramSocket(port, InetAddress.getByName(ip));
            this.isRunning = true;
            this.start();
            System.out.printf("Started %s server at %s on port %d%n", protocol,ip,port);
        }catch (Throwable throwable){
            throwable.printStackTrace();
            this.isRunning = false;
        }
    }

    @Override
    public void run() {
        super.run();

        while (isRunning){
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            try {
                try {
                    datagramSocket.receive(packet);
                    byte[] data = packet.getData();
                    this.onReceive(packet.getAddress().getHostAddress(),packet.getPort(),new String(data,packet.getOffset(),packet.getLength()));
                } catch (IOException ignored) {
                    this.isRunning = false;
                }
            }catch (Throwable throwable){
                throwable.printStackTrace();
            }

        }

        this.datagramSocket = null;
        System.out.printf("%s server stopped%n", protocol);
        this.interrupt();
    }

    @Override
    public void stopServer(){
        this.isRunning = false;
        this.datagramSocket.close();
    }

    @Override
    public void send(String ip,int port,String message){
        byte[] sendBuffer = message.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(sendBuffer, sendBuffer.length,InetAddress.getByName(ip),port);
            datagramSocket.send(packet);
        }catch (Throwable throwable){
         throwable.printStackTrace();
        }
    }

    @Override
    public void onReceive(String ip, int port, String message) {
        this.receiver.onReceive(ip,port,message);
    }


}
