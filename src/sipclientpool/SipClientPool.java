package sipclientpool;

import logger.Logger;
import server.NetworkMessageReceiver;
import sip.client.ClientController;
import sip.client.SipClient;
import tcp.TcpServer;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SipClientPool extends Thread implements NetworkMessageReceiver {
    private ConcurrentHashMap<String, SipClient> clients = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Long> clientIdles = new ConcurrentHashMap<>();
    private int maxIdle = 10;
    private TcpServer controlServer;
    private int controlServerPort;

    private String localIp;
    private String protocol;

    private String serverIp;
    private int serverPort;

    private Logger logger;

    private boolean isRunning;

    public SipClientPool() {
    }

    public void startPool(String localIp, String serverIp, int serverPort, int controlServerPort, String protocol) {
        this.localIp = localIp;
        this.protocol = protocol;
        this.controlServerPort = controlServerPort;

        this.serverIp = serverIp;
        this.serverPort = serverPort;

        this.controlServer = new TcpServer(localIp, this.controlServerPort, this);
        this.controlServer.startServer();

        this.logger = new Logger("sip_client_pool.log");
        this.isRunning = true;
        this.start();
        this.log(String.format("Sip client pool started with tcp control server at %s:%d%n", this.localIp, this.controlServerPort));
    }

    public void stopPool() {
        this.controlServer.stopServer();
        this.isRunning = false;
    }

    public SipClient spawnClient(String login) {
        SipClient client = new SipClient(localIp, login, false);
        client.connect(this.serverIp, this.serverPort, this.protocol);
        client.startClient();
        client.onStop = this::utilizeClient;

        this.clientIdles.put(login, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        this.clients.put(login, client);

        return client;
    }

    @Override
    public void onReceive(String ip, int port, String message) {
        try {
            switch (message.trim().toLowerCase()) {
                case "spawn":
                    SipClient client = spawnClient("ABOBA");
                    ClientController.processCommand(client, "register", null);
                    Thread.sleep(2000L);
                    ClientController.processCommand(client, "call", new String[]{"ff3"});
                    break;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        try {
            while (isRunning) {
                this.utilizeClients();
                Thread.sleep(200L);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    public void utilizeClients() {
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        Iterator<Map.Entry<String, SipClient>> clientIterator = this.clients.entrySet().iterator();
        while (clientIterator.hasNext()) {
            Map.Entry<String, SipClient> clientEntry = clientIterator.next();
            SipClient client = clientEntry.getValue();
            String login = client.getLogin();
            if (client.isBusy()) {
                this.clientIdles.put(login, currentTime);
            } else {
                if ((currentTime - this.clientIdles.get(login)) > maxIdle) {
                    if (client.isRunning()) {
                        this.log(String.format("Sip client %s has been utilized%n", login));
                        client.stopClient();
                    }
                }
            }
        }
    }

    private void utilizeClient(SipClient client) {
        this.clients.remove(client.getLogin());
        this.clientIdles.remove(client.getLogin());
    }

    private void log(String message) {
        System.out.println(message);
        this.logger.write(message);
    }
}
