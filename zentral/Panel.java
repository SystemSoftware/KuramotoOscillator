import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Panel extends JPanel implements Runnable{
	public static final double PI = 3.1416;
	public static final double K = 2; 

	public static final int oscillators = 30;
	public static final double maxFreq = 5;
	
	public Oscillator[] points;
	public int[] ring;
	public static final double speed = 0.3;
	
	public Panel(int[] ringCoordsX_Y_W_H){
		ring = ringCoordsX_Y_W_H;
		setOscillators();
		repaint();
		new Thread(this).start();
	}
	public void setOscillators(){
		double[] initPoints = new double[oscillators];
		double[] initFreq   = new double[oscillators];
 		points = new Oscillator[initPoints.length];
		for(int i=0; i<points.length; i++){
			initPoints[i] = Math.random() * PI * 2;
			initFreq[i] = Math.random() * maxFreq;
			points[i] = new Oscillator(initFreq[i], initPoints[i], this);
		}
	}
	public double[] getCoordsFromAngle(double angle){
		return new double[]{Math.cos(angle), Math.sin(angle)};
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
		w.add(new Panel(new int[]{90,90,200,200}));
		w.setSize(400, 400);
		w.setResizable(false);
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		w.setVisible(true);
		return w;
	}

	public void run() {
		long now = System.nanoTime();
		while(true){
			for(Oscillator e: points){
				e.tick();
			}
			this.repaint();
			long diff = System.nanoTime() - now;
			try { 
				if((diff/1000000) <= 16)
				Thread.sleep(16 - (diff/1000000));
			} catch (InterruptedException e) {}
			now = System.nanoTime();
		}
	}
	
	public static void main(String []a){
		createWindow();
	}
}
