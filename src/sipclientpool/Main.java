package sipclientpool;

public class Main {
    public static void main(String[] args){
        SipClientPool pool = new SipClientPool("10.0.43.19",5505,"10.0.43.19",9876,"tcp");
        pool.startPool();
    }
}
