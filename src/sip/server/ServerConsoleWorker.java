package sip.server;

import consoleworker.AbstractConsoleWorker;

public class ServerConsoleWorker extends AbstractConsoleWorker {

    private final SipServer server;

    public ServerConsoleWorker(SipServer server){
        this.server = server;
    }

    protected void processCommand(String command,String[] args){
        switch (command.toLowerCase()){
            case "stop":
                this.server.stopServer();
                break;
            case "show_users":
                this.server.security.getUsers().forEach((login,user)-> System.out.printf("%s - %s:%d%n",login,user.getHost(),user.getPort() ));
                break;
            case "call":
                if(args.length == 2){
                    //call once

                }else if(args.length == 4){
                    //call constantly with delays
                }
        }
    }

    private void makeCall(String login){
        if(server.security.hasUser(login)){

        }else{
            System.out.printf("User %s does not exist%n", login);
        }
    }
}
