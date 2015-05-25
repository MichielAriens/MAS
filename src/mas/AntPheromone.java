package mas;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class AntPheromone implements RoadUser, TickListener{
	
	private static final long DEFAULT_MAX_AGE = 10000000;
	
	private RandomGenerator rnd;
	private Point pos;
	private long timeToLive;
	private RoadModel roadModel;
	private AntPheromone parent;
	private List<AntPheromone> children = new ArrayList<AntPheromone>();
	private long weight = 1;
	
	public AntPheromone(Point pos, RandomGenerator rnd, long maxAge) {
		this.rnd = rnd;
		this.timeToLive = maxAge;
		this.pos = pos;
	}
	
	public AntPheromone(Point pos, RandomGenerator rnd) {
		this(pos, rnd, DEFAULT_MAX_AGE);
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		if(timeToLive <= 0){
			roadModel.removeObject(this);
		}
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		if(timeToLive > 0){
			timeToLive -= timeLapse.getTimeConsumed();
		}
	}

	@Override
	public void initRoadUser(RoadModel model) {
		roadModel = model;
		roadModel.addObjectAt(this, pos);
	}

	public long getTimeToLive(){
		return timeToLive;
	}
	
	public void addChild(AntPheromone child){
		if(child != null){
			this.children.add(child);
			child.parent = this;
		}
	}
	
	public AntPheromone getParent(){
		return this.parent;
	}
	
	public long getWeight(){
		return weight;
	}
	
	public void setWeight(long weight){
		this.weight = weight;
	}
	
	/**
	 * Choose a random child based on the weights of the pheromones provided.
	 * @return
	 */
	public AntPheromone getAChild(){
		if(children.isEmpty()){
			return null;
		}
		
		long[] weights = new long[children.size()];
		int i = 0;
		for(AntPheromone p : this.children){
			weights[i++] = p.getWeight();
		}
		for(i = 1; i < children.size(); i++){
			weights[i] = weights[i-1] + weights[i]; 
		}
		float x = (rnd.nextFloat() * weights[children.size() - 1]);
		i = 0;
		while(weights[i] < x){
			i++;
		}
		return children.get(i);
	}
}
