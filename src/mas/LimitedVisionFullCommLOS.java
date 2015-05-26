package mas;

import com.github.rinde.rinsim.core.model.road.RoadModel;

public class LimitedVisionFullCommLOS extends SimpleLimitedLOS {

	public LimitedVisionFullCommLOS(float viewDist, RoadModel rm) {
		super(viewDist, Float.MAX_VALUE, rm);
	}

}
