public class Oscillator
{
  public double val;
  private final double freq;
  private Panel parent;
  
  public Oscillator(double freq, double initVal, Panel parent)
  {
    this.freq = freq;
    this.val = initVal;
    this.parent = parent;
  }
  
  public void tick()
  {
    double sum = 0.0D;
    for (int i = 0; i < this.parent.points.length; i++) {
      sum += Math.sin(this.parent.points[i].val - this.val);
    }
    sum *= parent.K / this.parent.points.length;
    
    this.val += (this.freq + sum) / 62.5D * parent.speed * parent.PI % parent.PI;
  }
}
