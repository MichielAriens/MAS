package mas;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class PheromoneNode implements RoadUser{
	
	private RandomGenerator random;
	private Point position;
	private List<PheromoneEdge> edges = new ArrayList<PheromoneEdge>();
	
	public PheromoneNode(Point position, RandomGenerator random) {
		this.position = position;
		this.random = random;
	}

	@Override
	public void initRoadUser(RoadModel model) {
		model.addObjectAt(this, position);
	}
	
	public void addEdge(PheromoneEdge edge){
		if(!edge.getStart().equals(this)){
			return;
		}
		if(this.edges.contains(edge)){
			return;
		}
		this.edges.add(edge);
	}
	
	/**
	 * Choose a random edge based on the weights of the pheromones provided.
	 * @param fireFighter 
	 * @return
	 */
	public PheromoneEdge chooseEdge(AntFireFighter fireFighter){
		if(edges.isEmpty()){
			return null;
		}
		double[] weights = new double[edges.size()];
		int i = 0;
		for(PheromoneEdge p : this.edges){
			weights[i++] = p.getWeight(fireFighter, this);
		}
		for(i = 1; i < edges.size(); i++){
			weights[i] = weights[i-1] + weights[i]; 
		}
		double x = (random.nextDouble() * weights[edges.size() - 1]);
		i = 0;
		while(weights[i] < x){
			i++;
		}
		return edges.get(i);
	}
	
	public void promote(PheromoneEdge edge, double d){
		if(edges.contains(edge)){
			for(PheromoneEdge e : edges){
				if(e.equals(edge)){
					e.updateWeight(d);
				}else{
					e.updateWeight(-d);
				}
			}
		}else{
			System.out.println("test");
			throw new RuntimeException("bug");
		}
	}
	
	public Point getPosition(){
		return this.position;
	}
	
	@Override
	public String toString() {
		return "node: " + position.toString();
	}
	
	public List<PheromoneEdge> allEdges(){
		return this.edges;
	}
	
}
