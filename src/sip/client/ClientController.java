package sip.client;

import org.apache.hc.client5.http.fluent.Request;
import sip.SipRequest;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ClientController {

    public static void processCommand(SipClient client,String command, String[] args) {
        try {
            String toLogin;
            Dialog dialog;
            switch (command) {
                case "register":
                    SipRequest request = RequestRepository.register(client.hostIp, client.hostPort, client.login, client.clientIp, client.clientPort, client.fromTag, client.branchId, client.protocol);
                    client.send(client.hostIp, client.hostPort, request.toString());
                    break;
                case "call":
                    if (args.length != 1) {
                        client.log("Specify only username to whom you are calling");
                        break;
                    }
                    toLogin = args[0];

                    dialog = client.createDialog(toLogin);
                    dialog.startDialog();
                    dialog.inviteRecipient();
                    break;
                case "call_loop":
                    if (args.length != 4) {
                        client.log("Error!please type login calls interval duration");
                        break;
                    }
                    toLogin = args[0];
                    int calls = Integer.parseInt(args[1]);
                    int interval = Integer.parseInt(args[2]);
                    int duration = Integer.parseInt(args[3]);

                    client.log(String.format("Calling %s %d times for %d seconds with interval of %d", toLogin,calls,duration,interval));

                    int attempts = 0;
                    while((attempts <= calls)){
                        attempts++;
                        dialog = client.createDialog(toLogin);
                        dialog.startDialog();
                        informBackend(client,toLogin,true);
                        Thread.sleep(3000L);
                        dialog.inviteRecipient();
                        Thread.sleep(duration * 1000L);
                        if(dialog.isRunning()){
                            dialog.finishDialog();
                            informBackend(client,toLogin,false);
                        }
                        Thread.sleep(interval * 1000L);
                    }
                    break;
            }
        } catch (Throwable throwable) {
            StringWriter errorCh = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errorCh));
            client.log(errorCh.toString());
        }

    }

    private static void informBackend(SipClient client,String recipient,boolean callStarted){
        try{
            int callStatus = callStarted ? 1 : 0;
            String url = String.format("https://smarthome.citylink.pro/api/push/send_message/sip?sip_login=%s&message_type=1&call_started=%d", recipient,callStatus);

            client.log(String.format("Sending push, using %s", url));

            int httpCode = Request.get(url).execute().returnResponse().getCode();
            if(httpCode > 299){
                client.log(String.format("Could not send push, server responded with %d httpCode", httpCode));
            }
        }catch (Throwable throwable){
            StringWriter errorCh = new StringWriter();
            throwable.printStackTrace(new PrintWriter(errorCh));
            client.log(errorCh.toString());
        }

    }
}
