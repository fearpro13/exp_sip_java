package sip.client;

import consoleworker.AbstractConsoleWorker;
import org.apache.hc.client5.http.fluent.Request;
import sip.SipRequest;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ClientConsoleWorker extends AbstractConsoleWorker {

    private final SipClient client;

    public ClientConsoleWorker(SipClient client) {
        this.client = client;
    }

    @Override
    public void processCommand(String command, String[] args) {
        try {
            String toLogin;
            Dialog dialog;
            switch (command) {
                case "register":
                    SipRequest request = RequestRepository.register(client.hostIp, client.hostPort, client.login, client.clientIp, client.clientPort, client.fromTag, client.branchId, this.client.protocol);
                    this.client.send(client.hostIp, client.hostPort, request.toString());
                    break;
                case "call":
                    if (args.length != 1) {
                        log("Specify only username to whom you are calling");
                        break;
                    }
                    toLogin = args[0];

                    dialog = client.createDialog(toLogin);
                    dialog.startDialog();
                    dialog.inviteRecipient();
                    break;
                case "call_loop":
                    if (args.length != 4) {
                        log("Error!please type login calls interval duration");
                        break;
                    }
                    toLogin = args[0];
                    int calls = Integer.parseInt(args[1]);
                    int interval = Integer.parseInt(args[2]);
                    int duration = Integer.parseInt(args[3]);

                    log(String.format("Calling %s %d times for %d seconds with interval of %d", toLogin,calls,duration,interval));

                    int attempts = 0;
                    while((attempts <= calls)){
                        attempts++;
                        dialog = client.createDialog(toLogin);
                        dialog.startDialog();
                        informBackend(toLogin,true);
                        Thread.sleep(3000L);
                        dialog.inviteRecipient();
                        Thread.sleep(duration * 1000L);
                        if(dialog.isRunning()){
                            dialog.finishDialog();
                            informBackend(toLogin,false);
                        }
                        Thread.sleep(interval * 1000L);
                    }
                    break;
            }
        } catch (Throwable throwable) {
            StringWriter errorCh = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errorCh));
            log(errorCh.toString());
        }

    }

    private void log(String message){
        this.client.log(message);
    }

    private void informBackend(String recipient,boolean callStarted){
        try{
            int callStatus = callStarted ? 1 : 0;
            String url = String.format("https://smarthome.citylink.pro/api/push/send_message/sip?sip_login=%s&message_type=1&call_started=%d", recipient,callStatus);

            log(String.format("Sending push, using %s", url));

            int httpCode = Request.get(url).execute().returnResponse().getCode();
            if(httpCode > 299){
                log(String.format("Could not send push, server responded with %d httpCode", httpCode));
            }
        }catch (Throwable throwable){
            StringWriter errorCh = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errorCh));
            log(errorCh.toString());
        }

    }
}
