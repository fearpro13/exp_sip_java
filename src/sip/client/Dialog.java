package sip.client;

import sip.AbstractSipStructure;
import sip.SipRequest;
import sip.SipResponse;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Dialog extends Thread {
    private boolean isRunning;

    private SipClient client;

    private String recLogin;
    private String recIp;
    private int recPort;

    private String toTag;
    private String fromTag;

    private boolean invited;
    private boolean recInvited;

    private boolean tried;
    private boolean recTried;

    private boolean called;
    private boolean recCalled;

    private boolean established;
    private boolean recEstablished;

    private boolean finished;
    private boolean recFinished;

    private int timeout = 15;
    private long lastActivity;
    private long currentTime;
    private long idle;

    protected Consumer<Dialog> onFinish;

    private volatile Vector<SipRequest> requestQueue = new Vector<>();
    private volatile Vector<SipResponse> responseQueue = new Vector<>();

    public Dialog(SipClient client, String receiverLogin) {
        this.client = client;
        this.currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        this.lastActivity = currentTime;

        this.fromTag = client.fromTag;

        this.recLogin = receiverLogin;
        this.recIp = client.hostIp;
        this.recPort = client.hostPort;
    }

    public static Dialog createDialog(SipClient client, String receiverLogin) {
        Dialog dialog = new Dialog(client, receiverLogin);

        return dialog;
    }

    public void startDialog() {
        this.isRunning = true;
        this.start();
    }

    public void inviteRecipient() {
        if (!recInvited) {
            SipRequest request = RequestRepository.invite(
                    client.login, recLogin, recIp, recPort, client.clientIp, client.clientPort, client.branchId, fromTag, null, client.protocol
            );
            this.requestQueue.add(request);
            client.send(recIp, recPort, request.toString());
        }
    }

    private void ringRecipient() {
        if (!recCalled) {
            this.renameDialog(String.valueOf(Math.random() * 900000));
            SipResponse response = ResponseRepository.ringing(
                    client.login, recLogin, recIp, recPort, client.clientIp, client.clientPort, client.branchId, fromTag, this.toTag, client.protocol
            );
            this.responseQueue.add(response);
            client.send(recIp, recPort, response.toString());
        }
    }

    private void establishRecipient() {
        if (!recEstablished) {
            SipResponse response = ResponseRepository.ok(
                    client.login, recLogin, recIp, recPort, client.clientIp, client.clientPort, client.branchId, fromTag, this.toTag, client.protocol
            );
            this.responseQueue.add(response);
            client.send(recIp, recPort, response.toString());
        }
    }

    private void byeRecipient() {
        if (!recFinished) {
            SipRequest request = RequestRepository.bye(
                    client.login, recLogin, recIp, recPort, client.clientIp, client.clientPort, client.branchId, fromTag, this.toTag, client.protocol
            );
            this.requestQueue.add(request);
            client.send(recIp, recPort, request.toString());
        }
    }

    private void cancelRecipient(){
        SipRequest request = RequestRepository.cancel(
                client.login, recLogin, recIp, recPort, client.clientIp, client.clientPort, client.branchId, fromTag, this.toTag, client.protocol
        );
        this.requestQueue.add(request);
        client.send(recIp,recPort,request.toString());
    }

    private void ackInvite() {
        if (established && recEstablished) {
            SipRequest request = RequestRepository.ack(
                    client.login, recLogin, recIp, recPort, client.clientIp, client.clientPort, client.branchId, fromTag, this.toTag, client.protocol
            );
            client.send(recIp, recPort, request.toString());
            this.requestQueue.add(request);
        }
    }

    private void ack(){
        SipRequest request = RequestRepository.ack(
                client.login, recLogin, recIp, recPort, client.clientIp, client.clientPort, client.branchId, fromTag, this.toTag, client.protocol
        );
        client.send(recIp, recPort, request.toString());
        this.requestQueue.add(request);
    }

    private void okBye(String realRecIp,int realRecPort){
        SipResponse response = ResponseRepository.ok(
                client.login, recLogin, realRecIp, realRecPort, client.clientIp, client.clientPort, client.branchId,this.toTag,fromTag, client.protocol
        );
        client.send(recIp, recPort, response.toString());
        isRunning = false;
    }

    public void finishDialog() {
        byeRecipient();
        this.isRunning = false;
    }

    private void informTimeoutReached() {

    }

    private void renameDialog(String newName){
        client.renameDialog(this,newName);
        this.toTag = newName;
    }

    public void processRequest(SipRequest request) {
        this.lastActivity = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        if (request.getMethod().equalsIgnoreCase(AbstractSipStructure.METHODS.BYE.name())) {
            log("Received BYE from recipient");
            this.okBye(request.getRemoteIp(),request.getRemotePort());
            return;
        }

            if (invited && recInvited) {
                if (tried && recTried) {
                    if (called && recCalled) {
                        if (established && recEstablished) {
                            if (finished && recFinished) {

                            } else {

                            }
                        } else {
                        }
                    } else {

                    }
                } else {

                }
            } else {
                if (request.getMethod().equalsIgnoreCase(AbstractSipStructure.METHODS.INVITE.name())) {
                    if (request.getFromName().equals(recLogin)) {
                        setRecInvited();
                        setInvited();
                        this.ringRecipient();
                        this.establishRecipient();
                    }
                    if (request.getFromName().equals(client.login)) {
                        setInvited();
                    }
                }

            }
    }

    public void processResponse(SipResponse response) {
        this.lastActivity = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        if (invited && recInvited) {
            if (tried && recTried) {
                if (called && recCalled) {
                    if (established && recEstablished) {
                        if (finished && recFinished) {
                            isRunning = false;
                        } else {
                            if (response.getFromName().equalsIgnoreCase(client.login) && response.getStatusCode() > 400) {
                                log(String.format("Received %d %s from recipient", response.getStatusCode(), response.getReason()));
                                this.ack();
                            }
                        }
                    } else {
                        if (response.getFromName().equalsIgnoreCase(client.login) && response.getStatusCode() == 200) {
                            setRecEstablished();
                            setEstablished();
                            this.ackInvite();
                        }
                        if (response.getFromName().equalsIgnoreCase(client.login) && response.getStatusCode() > 400) {
                            log(String.format("Received %d %s from recipient%n", response.getStatusCode(), response.getReason()));
                            this.ack();
                        }
                    }
                } else {
                    if (response.getStatusCode() == 180 && response.getFromName().equalsIgnoreCase(client.login)) {
                        setRecCalled();
                        setCalled();
                        this.renameDialog(response.getToTag());
                    }
                }
            } else {
                if (response.getStatusCode() == 100 && response.getFromName().equalsIgnoreCase(client.login)) {
                    setTried();
                    setRecTried();
                }
                if (response.getStatusCode() == 180 && response.getFromName().equalsIgnoreCase(client.login)) {
                    setRecCalled();
                    setCalled();
                    this.toTag = response.getToTag();
                }
            }
        } else {
            if (response.getStatusCode() == 100 && response.getFromName().equalsIgnoreCase(client.login)) {
                setRecInvited();
                setRecTried();
                setTried();
            }
        }
    }

    public void processQueue() {
        if (!requestQueue.isEmpty()) {
            Iterator<SipRequest> reqIt = requestQueue.iterator();
            while (reqIt.hasNext()) {
                processRequest(reqIt.next());
                reqIt.remove();
            }
        }
        if (!responseQueue.isEmpty()) {
            Iterator<SipResponse> resIt = responseQueue.iterator();
            while (resIt.hasNext()) {
                processResponse(resIt.next());
                resIt.remove();
            }
        }
    }

    @Override
    public void run() {
        super.run();

        while (isRunning) {
            this.currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            this.idle = this.currentTime - this.lastActivity;

            processQueue();

            if (idle > this.timeout) {
                this.informTimeoutReached();

                log(String.format("Dialog %s has been reached maximum timeout of %d seconds %n", getDialogId(), this.timeout));
                isRunning = false;
                this.interrupt();
            }

            try {
                Thread.sleep(50L);
            } catch (InterruptedException ignored) {
            }
        }

        this.onFinish.accept(this);
        log(String.format("Dialog %s has been finished%n", getDialogId()));
        this.toTag = null;
        //this.interrupt();
    }

    public String getDialogId() {
        return toTag;
    }

    public Dialog setDialogId(String dialogId) {
        this.toTag = dialogId;

        return this;
    }

    private boolean isInvited() {
        return invited;
    }

    private void setInvited() {
        this.invited = true;
        log(String.format("dialog#%s@Client invited%n", getDialogId()));
    }

    private boolean isRecInvited() {
        return recInvited;
    }

    private void setRecInvited() {
        this.recInvited = true;
        log(String.format("dialog#%s@Recipient invited%n", getDialogId()));
    }

    private boolean isTried() {
        return tried;
    }

    private void setTried() {
        this.tried = true;
        log(String.format("dialog#%s@Client Tried%n", getDialogId()));
    }

    private boolean isRecTried() {
        return recTried;
    }

    private void setRecTried() {
        this.recTried = true;
        log(String.format("dialog#%s@Recipient Tried%n", getDialogId()));
    }

    private boolean isCalled() {
        return called;
    }

    private void setCalled() {
        this.called = true;
        log(String.format("dialog#%s@Client called%n", getDialogId()));
    }

    private boolean isRecCalled() {
        return recCalled;
    }

    private void setRecCalled() {
        this.recCalled = true;
        log(String.format("dialog#%s@Recipient called%n", getDialogId()));
    }

    private boolean isEstablished() {
        return established;
    }

    private void setEstablished() {
        this.established = true;
        log(String.format("dialog#%s@Client Established%n", getDialogId()));
    }

    private boolean isRecEstablished() {
        return recEstablished;
    }

    private void setRecEstablished() {
        this.recEstablished = true;
        log(String.format("dialog#%s@Recipient Established%n", getDialogId()));
    }

    private boolean isFinished() {
        return finished;
    }

    private void setFinished(boolean finished) {
        this.finished = finished;
        log(String.format("dialog#%s@Client finished%n", getDialogId()));
    }

    private boolean isRecFinished() {
        return recFinished;
    }

    private void setRecFinished(boolean recFinished) {
        this.recFinished = recFinished;
        log(String.format("dialog#%s@Recepient finished%n", getDialogId()));
    }

    private void log(String message){
        this.client.log(message);
    }

    public boolean isRunning(){
        return this.isRunning;
    }
}
