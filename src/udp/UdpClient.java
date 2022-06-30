package udp;

import server.NetworkMessageReceiver;
import server.TransportClientInterface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClient extends Thread implements TransportClientInterface {
    private final DatagramSocket datagramSocket;
    private final NetworkMessageReceiver receiver;

    private final byte[] recvBuffer = new byte[2048];

    private boolean isRunning;

    public UdpClient(DatagramSocket datagramSocket, NetworkMessageReceiver receiver) {
        this.receiver = receiver;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void onReceive(String ip, int port, String message) {
        this.receiver.onReceive(ip, port, message);
    }

    @Override
    public void send(String ip, int port, String message) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(ip), port);

            this.datagramSocket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        this.isRunning = true;
        super.start();
    }

    @Override
    public void run() {
        super.run();

        while (isRunning) {
            DatagramPacket packet = new DatagramPacket(recvBuffer, recvBuffer.length);

            try {
                datagramSocket.receive(packet);
                byte[] data = packet.getData();
                this.onReceive(packet.getAddress().getHostAddress(), packet.getPort(), new String(data, packet.getOffset(), packet.getLength()));

                Thread.sleep(100L);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        this.interrupt();
    }

    @Override
    public void stopClient() {
        datagramSocket.close();
        this.isRunning = false;
    }
}
