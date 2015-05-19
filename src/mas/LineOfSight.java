package mas;

import com.github.rinde.rinsim.core.model.road.RoadUser;

public abstract class LineOfSight {
	
	abstract boolean canSee(RoadUser u1, RoadUser u2);
	abstract boolean caComm(RoadUser u1, RoadUser u2);

}
