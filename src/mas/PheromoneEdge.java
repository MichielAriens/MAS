package mas;

import java.text.DecimalFormat;
import java.util.List;

import com.github.rinde.rinsim.geom.Point;

/**
 * A single direction edge going from point A to point B. 
 *
 */
public class PheromoneEdge {
	
	
	private static final double DEFAULT_STARTING_STRENGTH = 0.5;
	private static final double FIRE_STRENGTH_DILUTION = 0.5;
	
	private PheromoneNode a;
	private PheromoneNode b;
	
	private double pheromoneStrength;
	
	public PheromoneEdge(PheromoneNode a, PheromoneNode b){
		this.a = a;
		this.b = b;
		pheromoneStrength = DEFAULT_STARTING_STRENGTH;
		
		a.addEdge(this);
	}
	
	public String getPheromone(){
		DecimalFormat twoDecimals = new DecimalFormat("#.#");
        return twoDecimals.format(this.pheromoneStrength);
		//return this.pheromoneStrength;
	}
	
	public double getWeight(AntFireFighter fireFighter, PheromoneNode comingFrom){
		List<Point> fires = fireFighter.getVisibleFire();
		if(fires.isEmpty()){
			return this.pheromoneStrength;
		}
		double fireWeigth = 0;
		double oldFireWeight = 0;
		for(Point p : fires){
			fireWeigth += Point.distance(getOtherNode(comingFrom).getPosition(), p);
			oldFireWeight += Point.distance(comingFrom.getPosition(), p);
		}
		double ratio = oldFireWeight - fireWeigth;
		ratio = ratio / fires.size();
		System.out.println(ratio + " | " + this.pheromoneStrength );
		return Math.max(0, this.pheromoneStrength + ratio);
	}
	
	public void updateWeight(double d){
		this.pheromoneStrength += d*(1-this.pheromoneStrength);
		if(this.pheromoneStrength < 0){
			this.pheromoneStrength = 0;
		}
	}
	
	public PheromoneNode getOtherNode(PheromoneNode node){
		if(node.equals(a)){
			return b;
		}else if(node.equals(b)){
			return a;
		}else{
			return null;
		}
	}
	
	public double getDistance(){
		return Point.distance(a.getPosition(), b.getPosition());
	}
	
	public PheromoneNode getStart(){
		return a;
	}

	@Override
	public String toString() {
		return "edge:( " + a.toString() + ", " + b.toString() + ")";
	}
	
	public Point getPointAllong(float x){
		Point diff = Point.diff(a.getPosition(), b.getPosition());
		return new Point(a.getPosition().x - diff.x * x, a.getPosition().y - diff.y * x);
	}

}
