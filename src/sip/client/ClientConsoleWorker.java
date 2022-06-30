package sip.client;

import consoleworker.AbstractConsoleWorker;

public class ClientConsoleWorker extends AbstractConsoleWorker {
    private final SipClient sipClient;

    public ClientConsoleWorker(SipClient client) {
        this.sipClient = client;
    }

    @Override
    public void processCommand(String command, String[] args) {
        ClientController.processCommand(this.sipClient,command,args);
    }
}
