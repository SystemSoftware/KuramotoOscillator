public class Oscillator {
	public double val;
	private final double freq;
	private OscillatorGroup parent;
	
	public Oscillator(double freq, double initVal, OscillatorGroup parent){
		this.freq = freq;
		val = initVal;
		this.parent = parent;
	}
	public double tick(){
		//Kuramoto-Model
		double sum=0;
		for(int i=0; i< parent.points.length; i++){
			sum += Math.sin(parent.points[i].val - val);
		}
		sum *= ((OscillatorGroup.K) / ((double) parent.points.length));
		val += ((((freq+sum) / 62.5)*parent.speed) * (2 * OscillatorGroup.PI)) % (2 * OscillatorGroup.PI);
		return val;
 	}
}
