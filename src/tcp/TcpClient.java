package tcp;

import server.NetworkMessageReceiver;
import server.TransportClientInterface;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public class TcpClient extends Thread implements TransportClientInterface {
    private final Socket socket;
    private boolean isRunning;

    private PrintWriter out;
    private final byte[] messageBuffer = new byte[2048];

    protected Consumer<TcpClient> onShutdown;

    protected final NetworkMessageReceiver receiver;

    public TcpClient(Socket socket, NetworkMessageReceiver receiver) {
        this.socket = socket;
        this.receiver = receiver;
    }

    public void startClient() {

    }

    @Override
    public synchronized void start() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            this.isRunning = true;

            setName("tcp_client");
            super.start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        String ip = socket.getInetAddress().getHostAddress();
        int port = socket.getPort();
        int bytesRead;

        try {
            while (isRunning) {
                try {
                    bytesRead = socket.getInputStream().read(messageBuffer, 0, messageBuffer.length);
                    if(bytesRead == -1){
                        System.out.printf("Received end of stream from %s:%d%n", ip,port);
                        isRunning = false;
                    }
                    if (bytesRead != -1) {
                        String message = new String(messageBuffer, 0, bytesRead);
                        this.onReceive(ip, port, message);
                    }
                } catch (SocketException socketException) {
                    isRunning = false;
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            if (!socket.isClosed()) {
                throwable.printStackTrace();
            }

        }

        this.shutdown();
        //this.interrupt();
    }

    public void stopClient() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.isRunning = false;
    }

    private void shutdown() {
        this.isRunning = false;
        try {
            System.out.printf("%s:%s disconnected%n", socket.getInetAddress().getHostAddress(), socket.getPort());
            this.socket.close();

            if (this.onShutdown != null) {
                this.onShutdown.accept(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void send(String ip, int port, String message) {
        out.print(message);
        out.flush();
    }

    public void onReceive(String ip, int port, String message) {
        this.receiver.onReceive(this.socket.getInetAddress().getHostAddress(), this.socket.getPort(), message);
    }

    public Socket getSocket() {
        return socket;
    }
}
