package mas;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class AntPheromone implements RoadUser, TickListener{
	
	private static final long DEFAULT_MAX_AGE = 10000000;
	
	private Point pos;
	private long timeToLive;
	private RoadModel roadModel;
	
	public AntPheromone(Point pos, long maxAge) {
		this.timeToLive = maxAge;
		this.pos = pos;
	}
	
	public AntPheromone(Point pos) {
		this(pos, DEFAULT_MAX_AGE);
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

}
