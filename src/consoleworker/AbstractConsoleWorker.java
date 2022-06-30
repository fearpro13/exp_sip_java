package consoleworker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class AbstractConsoleWorker extends Thread{

    private BufferedReader in;
    private boolean isRunning;

    public void startWorker(){
        this.isRunning = true;
        in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Console worker started");
        this.start();
    }

    public void stopWorker(){
        this.isRunning = false;
    }

    @Override
    public void run(){
        super.run();

        String message;
        String command = null;
        String[] args = null;
        while (isRunning){
            try {
                if((message = in.readLine()) != null){
                    if(message.contains(" "))
                    {
                        try {
                            command = message.substring(0, message.indexOf(" "));
                            args = message.substring(message.indexOf(" ")+1).split(" ");
                        }catch (Throwable throwable){
                            throwable.printStackTrace();
                        }
                    }else{
                        command = message;
                        args = new String[]{};
                    }
                    processCommand(command,args);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Console worker stopped");
        in = null;
        this.interrupt();
    }

    protected abstract void processCommand(String command,String[] args);
}
