package mas;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class RefillStation implements RoadUser {
	private RoadModel roadModel;
	private Point position;
	
	public RefillStation(Point pos) {
		position = pos;
	}

	@Override
	public void initRoadUser(RoadModel model) {
		roadModel = model;
		roadModel.addObjectAt(this, position);
	}

}
