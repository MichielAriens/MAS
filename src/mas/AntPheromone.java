package mas;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class AntPheromone implements RoadUser, TickListener{
	
	private Point pos;
	private long timeToLive;
	private RoadModel roadModel;
	
	public AntPheromone(Point pos, long maxAge) {
		this.timeToLive = maxAge;
		this.pos = pos;
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

	public long getAge(){
		return timeToLive;
		
	}

}
