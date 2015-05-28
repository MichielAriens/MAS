package mas;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

public class ModelBuilder {
	
	private RoadModel model;
	private List<Point> refillStations = new ArrayList<>();
	private List<Point> fires = new ArrayList<>();
	
	
	public ModelBuilder(RoadModel initialModel){
		this.model = initialModel;
	}
	
	public void addRefillStations(List<Point> arg){
		refillStations.addAll(arg);
	}
	
	public void addFires(List<Point> arg){
		fires.addAll(arg);
	}
	
	
	private void build(int noAgents, RandomGenerator random){
		
	}
	
	public RoadModel getModel(){
		return this.model;
	}

}
