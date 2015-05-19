package mas;

import com.github.rinde.rinsim.core.model.road.RoadUser;

public class FullLineOfSight extends LineOfSight{

	@Override
	boolean canSee(RoadUser u1, RoadUser u2) {
		 return true;
	}

	@Override
	boolean canComm(RoadUser u1, RoadUser u2) {
		return true;
	}

}
