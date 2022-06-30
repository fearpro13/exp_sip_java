package server;

public abstract class AbstractServer extends Thread{
    protected boolean isRunning;
    protected String ip;
    protected int port;
    protected String protocol;

    abstract public void startServer();

    abstract public void stopServer();

    abstract public void send(String ip,int port,String message);

    abstract public void onReceive(String ip,int port,String message);

    public boolean isRunning(){
        return this.isRunning;
    }
}
