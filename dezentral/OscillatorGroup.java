import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;


public class OscillatorGroup extends Thread{
	public static final double PI = 3.1416;
	public static final double K = 4;
	public final int oscCount, groupNR, groupSize;
	public final double maxFreq = 5;
	public Oscillator[] points;

	public final double speed = 1;
	
	private Socket subscriber, pusher;
	
	public OscillatorGroup(int groupNR, int oscCount, int groupSize){
		points = new Oscillator[oscCount];
		this.oscCount = oscCount;
		this.groupNR = groupNR;
		this.groupSize = groupSize;
		for(int i = 0; i<points.length; i++){
			points[i] = new Oscillator(Math.random() * maxFreq, 0, this);
		}
		Context context = ZMQ.context(1);
        subscriber = context.socket(ZMQ.SUB);
        subscriber.connect("tcp://localhost:57353");
        subscriber.subscribe(new byte[0]);
        
		pusher = context.socket(ZMQ.PUSH);
		pusher.connect("tcp://localhost:57354");
				
        this.start();
	}
	
	public void run(){
		for(;;){
	    	 String msg = subscriber.recvStr();
	    	 String[] coords = msg.split(",");
	    	 for(int i=0; i<points.length; i++){
	    		 points[i].val = Double.parseDouble(coords[i]);
	    	 }
	    	 msg = "";
	    	 for(int i=groupNR*5; i<(groupNR*5)+5; i++){
	    		 msg = "" + msg + points[i].tick() + ",";
	    	 }
	    	 msg += groupNR;
	    	 pusher.send(msg);
	    	 try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
