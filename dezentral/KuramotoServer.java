import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.zeromq.*;
import org.zeromq.ZMQ.*;

public class KuramotoServer extends JPanel implements Runnable{
	public static final double PI = 3.1416;
	public static final double K = 10; 

	public static final int oscillators = 30;
	public static final double maxFreq = 5;
	private long now = 0;
	public Oscillator[] points;
	public int[] ring;
	public static final double speed = 0.1;
	
	public boolean canSend = true;
	private Socket publisher;
	public KuramotoServer(int[] ringCoordsX_Y_W_H){
			ring = ringCoordsX_Y_W_H;
			setOscillators();
			Context context = ZMQ.context(1);
	        publisher = context.socket(ZMQ.PUB);
	        publisher.bind("tcp://*:57353");
			new Thread(this).start();
			new recver().start();
	}
	public double[] getCoordsFromAngle(double angle){
		return new double[]{Math.cos(angle), Math.sin(angle)};
	}
	public void setOscillators(){
		double[] initPoints = new double[oscillators];
 		points = new Oscillator[initPoints.length];
		for(int i=0; i<points.length; i++){
			initPoints[i] = Math.random() * PI * 2;
			points[i] = new Oscillator(0, initPoints[i], null);
		}
	}
	public void run(){
		while(true){
		if(System.currentTimeMillis() - now > 5000){
			publish();
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
	public void publish(){
		now = System.currentTimeMillis();
        String msg = "";
        for(int i=0; i<points.length; i++){
        		msg += points[i].val + ",";
        }
        publisher.send(msg);
        repaint();
	}
	@Override
	public void paint(Graphics g){
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 400, 400);
		g.setColor(Color.BLACK);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2.setStroke(new BasicStroke(3));
		g2.drawOval(ring[0], ring[1], ring[2], ring[3]);
		g2.setStroke(new BasicStroke(1));
		g2.drawLine(ring[0] + ring[2]/2, 0, ring[1] + ring[3]/2, Integer.MAX_VALUE);
		g2.drawLine(0, ring[0] + ring[2]/2, Integer.MAX_VALUE, ring[1] + ring[3]/2);
		g2.setColor(new Color(30,160,0));
		double zN = 0.0, zI = 0.0;
		for(Oscillator e : points){
			double i = e.val;
			double[] coords = getCoordsFromAngle(i);
			zN += coords[0];
			zI += coords[1];
			g2.fillOval((int) ((ring[0] + ring[2]/2) + (coords[0] * (ring[2]/2))) - 3, (int) ((ring[1] + ring[3]/2) - (coords[1] * (ring[3]/2))) - 3, 10, 10);
		}
		g2.setColor(new Color(0,0,127));
		zN /= points.length;
		zI /= points.length;
		g2.fillOval((int) ((ring[0] + ring[2]/2) + (zN * (ring[2]/2))) - 3, (int) ((ring[1] + ring[3]/2) - (zI * (ring[3]/2))) - 3, 10, 10);
	}
	
	public static JFrame createWindow(){
		JFrame w = new JFrame("Kuramoto-Oszillator");
		w.setLayout(new BorderLayout());
		w.add(new KuramotoServer(new int[]{90,90,200,200}));
		w.setSize(400, 400);
		w.setResizable(false);
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
		return w;
	}
    public static void main(String[] args) throws Exception {
    	createWindow();
    	new OscillatorGroup(0,30,5);
    	new OscillatorGroup(1,30,5);
    	new OscillatorGroup(2,30,5);
    	new OscillatorGroup(3,30,5);
    	new OscillatorGroup(4,30,5);
    	new OscillatorGroup(5,30,5);
    }
    class recver extends Thread{
    	public void run(){
    		Context context = ZMQ.context(1);
            Socket puller = context.socket(ZMQ.PULL);
            puller.bind("tcp://*:57354");
            
    		String[] recvStr = new String[6];
    		String msg;
            while(true){
                msg = puller.recvStr();
                String[] msgs = msg.split(",");
                recvStr[Integer.parseInt(msgs[msgs.length-1])] = msg;
                int count = 0;
                for(int i=0; i<6; i++){
                	if(recvStr[i] != null) count++;
                }
                if(count == 6){
                	for(int i=0; i<points.length; i++){
                		points[i].val = Double.parseDouble(recvStr[i/5].split(",")[i%5]);
                	}
                	recvStr = new String[6];
                	publish();
                }
                try {
					Thread.sleep(2);
				} catch (InterruptedException e) {e.printStackTrace();}
            }
    	}
    }
}