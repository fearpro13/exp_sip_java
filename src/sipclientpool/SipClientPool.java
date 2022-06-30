package sipclientpool;

import logger.Logger;
import server.NetworkMessageReceiver;
import sip.client.SipClient;
import tcp.TcpServer;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class SipClientPool extends Thread implements NetworkMessageReceiver {
    private HashMap<String, SipClient> clients = new HashMap<>();
    private HashMap<String,Long> clientIdles = new HashMap<>();
    private int maxIdle = 30;
    private TcpServer controlServer;
    private int controlServerPort;

    private String localIp;
    private String protocol;

    private String serverIp;
    private int serverPort;

    private Logger logger;

    private boolean isRunning;

    public SipClientPool(String serverIp,int serverPort,String localIp,int controlServerPort,String protocol){
        this.localIp = localIp;
        this.protocol = protocol;
        this.controlServerPort = controlServerPort;

        this.serverIp = serverIp;
        this.serverPort = serverPort;

        this.controlServer = new TcpServer(localIp,this.controlServerPort,this);
    }

    public void startPool(){
        this.controlServer.startServer();
        this.logger = new Logger("sip_client_pool");
        this.isRunning = true;
        this.start();
        this.log(String.format("Sip client pool started with tcp control server at %s:%d", this.localIp,this.controlServerPort));
    }

    public void spawnClient(String login){
        SipClient client = new SipClient(localIp,login,false);
        client.startClient();
        client.connect(this.serverIp, this.serverPort, this.protocol);
        this.clients.put(login,client);
        this.clientIdles.put(login,TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    }

    @Override
    public void onReceive(String ip, int port, String message) {
        switch (message.trim().toLowerCase()){
            case "spawn":
                spawnClient("ABOBA");
                break;
        }
    }

    @Override
    public void run() {
        super.run();

        while (isRunning){
            long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            this.clients.forEach((login,client) -> {
                if(client.isBusy()){
                    this.clientIdles.put(login,currentTime);
                }else{
                    if((currentTime - this.clientIdles.get(login)) > maxIdle){
                        utilizeClient(login);
                    }
                }
            });
        }
    }

    public void utilizeClient(String login){
        this.log(String.format("Sip client %s has been utilized", login));
        SipClient client = this.clients.remove(login);
        client.stopClient();
    }

    private void log(String message){
        System.out.println(message);
        this.logger.write(message);
    }
}
