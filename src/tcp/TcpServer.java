package tcp;

import server.AbstractServer;
import server.NetworkMessageReceiver;
import server.TransportClientInterface;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TcpServer extends AbstractServer implements TransportClientInterface {
    private ServerSocket serverSocket;
    private final NetworkMessageReceiver receiver;

    private final HashMap<String, TcpClient> clients = new HashMap<>();

    public TcpServer(String ip, int port, NetworkMessageReceiver receiver) {
        this.ip = ip;
        this.port = port;
        this.protocol = "TCP";

        this.receiver = receiver;
    }

    @Override
    public void startServer() {
        try {
            this.serverSocket = new ServerSocket(port, 5, InetAddress.getByName(ip));
            this.isRunning = true;
            this.start();
            System.out.printf("Started %s server at %s on port %d%n", protocol, ip, port);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        while (isRunning) {
            try {
                try {
                    Socket client = serverSocket.accept();

                    String path = String.format("%s:%d", client.getInetAddress().getHostAddress(), client.getPort());
                    TcpClient tcpClient = new TcpClient(client, this);
                    clients.put(path, tcpClient);

                    tcpClient.onShutdown = this::disconnect;

                    tcpClient.start();
                } catch (IOException ignored) {
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }

        this.disconnectClients();
        this.serverSocket = null;
        System.out.printf("%s server stopped%n", protocol);
        this.interrupt();
    }

    private void disconnectClients(){
        Iterator<Map.Entry<String,TcpClient>> it = this.clients.entrySet().iterator();
        while (it.hasNext()){
            TcpClient client = it.next().getValue();
            client.stopClient();
            it.remove();
        }
    }

    @Override
    public void stopServer() {
        this.isRunning = false;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(String ip, int port, String message) {
        String path = String.format("%s:%d", ip, port);
        TcpClient client = clients.get(path);
        if (client != null) {
            client.send(ip, port, message);
        }
    }

    public void disconnect(TcpClient client) {
        if (client.isRunning()) {
            client.stopClient();
        }
        this.clients.remove(String.format("%s:%d", client.getSocket().getInetAddress().getHostAddress(), client.getSocket().getPort()));
    }

    @Override
    public void onReceive(String ip, int port, String message) {
        receiver.onReceive(ip, port, message);
    }
}
