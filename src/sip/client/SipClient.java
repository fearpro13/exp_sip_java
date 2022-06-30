package sip.client;

import logger.Logger;
import server.TransportClientInterface;
import sip.*;
import tcp.TcpClient;
import udp.UdpClient;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

public class SipClient extends Thread implements TransportClientInterface {
    protected String hostIp;
    protected int hostPort;
    protected String protocol;

    protected String clientIp;
    protected int clientPort;

    protected final String login;

    private final StringBuilder bufferedMessage = new StringBuilder();

    private BufferedReader in;

    private boolean isAlive;

    private PipedOutputStream sink;
    private PipedInputStream messageBuffer;

    public static String callId = "asfh3q4tygqa3ghy54u";
    public String fromTag;
    public String branchId;

    protected TransportClientInterface transportClient;

    private Logger logger;

    private HashMap<String, Dialog> dialogs = new HashMap<>();


    private boolean isBusy;

    //since 2022-June-16
    public static final String version = "0.0.14";
    public static final String userAgent = "Experimental Sip Client 0.0.14";


    private boolean needConsoleWorker = true;

    public SipClient(String localIp, String login,boolean needConsoleWorker) {
        this.login = login;
        this.clientIp = localIp;
        this.branchId = String.format("z9hG4bK-%s", Math.random() * 900000);
        this.fromTag = String.valueOf(Math.random() * 900000);
        SipClient.callId = String.valueOf(Math.random() * 900000);
        this.logger = new Logger(String.format("sip_client_%s.log", login));
        this.needConsoleWorker = needConsoleWorker;
    }

    public void startClient() {
        if(this.needConsoleWorker){
            ClientConsoleWorker consoleWorker = new ClientConsoleWorker(this);
            consoleWorker.startWorker();
        }

        sink = new PipedOutputStream();
        try {
            messageBuffer = new PipedInputStream(sink);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(String ip, int port, String protocol) {
        try {
            if (protocol.equalsIgnoreCase("tcp")) {
                Socket socket = new Socket();
                InetAddress address = InetAddress.getByName(ip);
                InetSocketAddress socketAddress = new InetSocketAddress(address, port);
                socket.connect(socketAddress);

                clientPort = socket.getLocalPort();
                clientIp = socket.getLocalAddress().getHostAddress();

                transportClient = new TcpClient(socket, this);
            } else if (protocol.equalsIgnoreCase("udp")) {
                DatagramSocket datagramSocket = new DatagramSocket();

                clientPort = datagramSocket.getLocalPort();

                transportClient = new UdpClient(datagramSocket, this);
            } else {
                throw new RuntimeException(String.format("%s protocol is not supported", protocol));
            }

            log(String.format("Successfully connected from %s:%d to %s:%d %s", clientIp, clientPort, ip, port, protocol));

            this.hostIp = ip;
            this.hostPort = port;
            this.protocol = protocol;

            isAlive = true;

            this.start();
            transportClient.start();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            isAlive = false;
        }

    }

    public void onReceive(String ip, int port, String message) {
        String serviceHeader = String.format("%s:%d#", ip, port);
        String fullMessage = (serviceHeader + message + "\r\n");
        try {
            sink.write(fullMessage.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String ip, int port, String message) {
        log(String.format("[USER -> SERVER] %s", message.split("\r\n")[0]));
        this.transportClient.send(ip, port, message);
    }

    @Override
    public void run() {
        super.run();

        try {
            in = new BufferedReader(new InputStreamReader(messageBuffer));

            String currentMessage;
            while (isAlive) {
                currentMessage = in.readLine();
                if (currentMessage.equals("")) {
                    String message = bufferedMessage.toString();
                    bufferedMessage.setLength(0);

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

                    log(String.format("[SERVER:%s:%d -> USER] %s%n", remoteIp,remotePort, message.split("\r\n")[0]));

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
                    bufferedMessage.append(currentMessage).append("\r\n");
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            isAlive = false;
        }

        this.log(String.format("Sip client %s stopped working", this.login));

    }

    private void parseRequest(SipRequest request) {
        try {
            if (request.getContentLength() > 0) {
                char[] bodyBuffer = new char[request.getContentLength()];
                int bytesRead = in.read(bodyBuffer, 0, request.getContentLength());
                request.setBody(new String(bodyBuffer, 0, bytesRead));
            }

            if (request.getToTag() != null) {
                Dialog dialog = this.dialogs.get(request.getToTag());
                if (dialog == null) {
                    if(
                            request.getMethod().equalsIgnoreCase(AbstractSipStructure.METHODS.BYE.name()) &&
                                    !String.format("%s:%d", request.getRemoteIp(),request.getRemotePort()).equalsIgnoreCase(String.format("%s:%d",hostIp,hostPort )) &&
                                    request.getToTag().equalsIgnoreCase(this.fromTag)
                    ){
                        //Recipient sends bye directly to client, avoiding proxy
                        //and has swapped FROM and TO headers. so that we have to search dialog by fromTag instead of toTag
                        dialog = this.dialogs.get(request.getFromTag());
                        dialog.processRequest(request);
                        return;
                    }

                    SipResponse response = ResponseRepository.dontExist(
                            this.login, request.getContactName(), hostIp, hostPort, clientIp, clientPort, request.getViaBranch(), request.getFromTag(), request.getToTag(), protocol
                    );
                    response.addCSeq(request.getcSecNumber(), request.getcSecMethod());
                    this.send(hostIp, hostPort, response.toString());
                } else {
                    dialog.processRequest(request);
                }
            } else {
                if (request.getMethod().equalsIgnoreCase(AbstractSipStructure.METHODS.INVITE.name())) {
                    Dialog dialog = this.createDialog(request.getFromName());
                    dialog.startDialog();
                    dialog.processRequest(request);
                }
            }
        } catch (Throwable throwable) {
            throwable.getStackTrace();
        }
    }

    private void parseResponse(SipResponse response) {
        try {
            if (response.getContentLength() > 0) {
                char[] bodyBuffer = new char[response.getContentLength()];
                int bytesRead = in.read(bodyBuffer, 0, response.getContentLength());
                response.setBody(new String(bodyBuffer, 0, bytesRead));
            }

            if (response.getToTag() != null) {
                Dialog dialog;
                if(response.getStatusCode() == 180){
                    dialog = dialogs.get(response.getToName());
                }else{
                    dialog = dialogs.get(response.getToTag());
                }

                if (dialog == null) {
                    SipResponse answer = ResponseRepository.dontExist(
                            this.login, response.getContactName(), hostIp, hostPort, clientIp, clientPort, response.getViaBranch(), response.getFromTag(), response.getToTag(), protocol
                    );
                    answer.addCSeq(response.getcSecNumber(), response.getcSecMethod());
                    this.send(hostIp, hostPort, answer.toString());
                } else {
                    dialog.processResponse(response);
                }
            } else {
                if (response.getStatusCode() == 100) {
                    Dialog dialog;
                    if ((dialog = this.dialogs.get(response.getToName())) != null) {
                        dialog.processResponse(response);
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public Dialog createDialog(String toLogin) {
        Dialog dialog = Dialog.createDialog(this, toLogin);
        dialog.onFinish = this::deleteDialog;
        dialog.setDialogId(toLogin);
        this.dialogs.put(toLogin, dialog);
        log(String.format("%s dialog created%n", toLogin));
        this.setBusy(true);

        return dialog;
    }

    public void renameDialog(Dialog dialog,String newName){
        String oldName = dialog.getDialogId();
        Dialog prevDialog = this.dialogs.get(oldName);
        if (prevDialog != null) {
            log(String.format("Dialog %s renamed to %s%n", oldName,newName));
            this.dialogs.put(newName,this.dialogs.remove(dialog.getDialogId()));
        }
    }

    private void deleteDialog(Dialog dialog) {
        if (dialog != null) {
            this.setBusy(false);
            this.dialogs.remove(dialog.getDialogId());
        }
    }

    public void finishDialog(String dialogId) {
        Dialog dialog = this.dialogs.get(dialogId);
        if (dialog != null) {
            dialog.finishDialog();
            this.deleteDialog(dialog);
        }
    }

    public void log(String message){
        System.out.println(message);
        this.logger.write(message);
    }

    public void stopClient(){
        this.isAlive = false;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }
}
