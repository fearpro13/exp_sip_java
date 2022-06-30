package sip.server;

import logger.Logger;
import server.AbstractServer;
import server.TransportClientInterface;
import sip.*;
import sip.router.Router;
import sip.security.Security;
import sip.security.User;
import tcp.TcpServer;
import udp.UdpServer;

import java.io.*;

public class SipServer extends Thread implements TransportClientInterface {
    //since 2022-JUNE-08
    public static final String version = "0.0.21";

    private boolean isRunning;

    public static String ip;
    public static int port;
    public static String protocol;

    private PipedInputStream messageBuffer;
    private PipedOutputStream sink;

    private String bufferedMessage = "";

    private Logger logger;
    private ServerConsoleWorker consoleWorker;

    private AbstractServer transportServer;

    protected volatile Security security;
    private Router router;

    private BufferedReader in;

    public SipServer() {
    }

    public void startClient(){
    }

    public void start(String ip, int port, String protocol) throws IOException {
        //since 2022-JUNE-08
        System.out.printf("Experimental sip server %s\n\n", version);

        security = new Security();
        this.router = new Router(this);
        this.isRunning = true;
        this.logger = new Logger("sip_server.log");
        this.consoleWorker = new ServerConsoleWorker(this);
        consoleWorker.startWorker();

        sink = new PipedOutputStream();
        messageBuffer = new PipedInputStream(sink);

        if (protocol.equalsIgnoreCase("tcp")) {
            transportServer = new TcpServer(ip, port, this);
        } else if (protocol.equalsIgnoreCase("udp")) {
            transportServer = new UdpServer(ip, port, this);
        } else {
            throw new RuntimeException(String.format("Protocol %s is not supported. Supported protocols are TCP,UDP", protocol));
        }

        SipServer.port = port;
        SipServer.ip = ip;
        SipServer.protocol = protocol.toUpperCase();

        transportServer.startServer();

        super.start();
        System.out.printf("Started SIP server at %s://%s:%d \n", protocol, ip, port);
    }

    public void stopServer() {
        try {
            this.sink.close();
            this.messageBuffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.isRunning = false;
        this.transportServer.stopServer();
        this.consoleWorker.stopWorker();

        this.transportServer = null;
        this.consoleWorker = null;
    }

    public void log(String message) {
        this.logger.write(message);
        this.logger.write("\n");
    }

    @Override
    public void onReceive(String ip, int port, String message) {
        String serviceHeader = String.format("%s:%d#", ip, port);
        String fullMessage = (serviceHeader + message + "\r\n");
        try {
            sink.write(fullMessage.getBytes());
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        super.run();

        in = new BufferedReader(new InputStreamReader(messageBuffer));
        try {
            String currentMessage;
            String message;

            while (isRunning && ((currentMessage = in.readLine()) != null)) {
                if (currentMessage.equals("")) {
                    message = bufferedMessage;
                    bufferedMessage = "";

                    if (message.length() == 0) {
                        continue;
                    }

                    int posOfSign = message.indexOf("#");

                    if (posOfSign == -1) {
                        continue;
                    }

                    String[] serviceHeader = message.substring(0, posOfSign).split(":");
                    String remoteIp = serviceHeader[0];
                    int remotePort = Integer.parseInt(serviceHeader[1]);

                    message = message.substring(posOfSign + 1);

                    String firstLogLine = "";
                    if(message.split("\r\n").length != 0){
                        firstLogLine = message.split("\r\n")[0];
                    }

                    User user;
                    if ((user = this.security.getUserByAddress(remoteIp, remotePort)) != null) {
                        this.log(String.format("[%s:%s:%d --> SERVER] %s", user.getLogin(), remoteIp, remotePort,firstLogLine));
                        System.out.printf("[%s:%s:%d --> SERVER] %s%n", user.getLogin(), remoteIp, remotePort,firstLogLine);
                    } else {
                        this.log(String.format("[%s:%d --> SERVER] %s", remoteIp, remotePort,firstLogLine));
                        System.out.printf("[%s:%d --> SERVER] %s%n", remoteIp, remotePort,firstLogLine);
                    }
                    this.log(message);

                    if (AbstractSipStructure.isRequest(message)) {
                        SipRequest incomingRequest = SipRequest.parse(message);
                        incomingRequest.setRemoteIp(remoteIp);
                        incomingRequest.setRemotePort(remotePort);

                        parseRequest(incomingRequest);
                    } else {
                        if (AbstractSipStructure.isResponse(message)) {
                            SipResponse incomingResponse = SipResponse.parse(message);
                            incomingResponse.setRemoteIp(remoteIp);
                            incomingResponse.setRemotePort(remotePort);

                            parseResponse(incomingResponse);
                        }
                    }
                } else {
                    bufferedMessage += (currentMessage + "\r\n");
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            stopServer();
        }

        System.out.println("SIP server stopped");
        this.interrupt();
    }

    private void parseRequest(SipRequest request) {
        try {
            if (request.getContentLength() > 0) {
                char[] bodyBuffer = new char[request.getContentLength()];
                 int readBytes = in.read(bodyBuffer, 0, request.getContentLength());
                request.setBody(new String(bodyBuffer,0,readBytes));
            }

            SipResponse response = router.process(request);
            if (response != null) {
                String responseRaw = response.toString();
                this.send(request.getRemoteIp(), request.getRemotePort(), responseRaw);
            }
        } catch (Throwable throwable) {
            throwable.getStackTrace();
        }
    }

    private void parseResponse(SipResponse response) {
        try {
            if (response.getContentLength() > 0) {
                char[] bodyBuffer = new char[response.getContentLength()];
                int readBytes = in.read(bodyBuffer, 0, response.getContentLength());
                response.setBody(new String(bodyBuffer,0,readBytes));
            }

            if (
                    this.security.hasUser(response.getToName()) &&
                            this.security.hasUser(response.getFromName())
            ) {
                User receiver;

                if (this.security.getUserByAddress(response.getRemoteIp(), response.getRemotePort()).getLogin().equals(response.getToName())) {
                    receiver = this.security.getUser(response.getFromName());
                } else {
                    receiver = this.security.getUser(response.getToName());
                }
                this.send(receiver.getRemoteIp(), receiver.getRemotePort(), response.toString());

            } else {
                SipResponse userNotFoundResponse = SipResponse.create(String.format("ExperimentalSipServer %s", SipServer.version));
                userNotFoundResponse.setStatusCode(404).setReason("User not found");

                this.send(response.getRemoteIp(), response.getRemotePort(), userNotFoundResponse.toString());
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void send(String ip, int port, String message) {
        String firstLogLine = "";
        if(message.split("\r\n").length != 0){
            firstLogLine = message.split("\r\n")[0];
        }

        User user;
        if ((user = this.security.getUserByAddress(ip, port)) != null) {
            this.log(String.format("[SERVER --> %s:%s:%d] %s", user.getLogin(), ip, port,firstLogLine));
            System.out.printf("[SERVER --> %s:%s:%d] %s%n", user.getLogin(), ip, port,firstLogLine);
        } else {
            this.log(String.format("[SERVER --> %s:%d] %s", ip, port,firstLogLine));
            System.out.printf("[SERVER --> %s:%d] %s%n", ip, port,firstLogLine);
        }
        this.log(message);

        transportServer.send(ip, port, message);
    }

    public void send(User user, String message) {
        this.send(user.getRemoteIp(), user.getRemotePort(), message);
    }

    public Security getSecurity() {
        return security;
    }
}
