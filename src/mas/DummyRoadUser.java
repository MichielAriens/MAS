package mas;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class DummyRoadUser implements RoadUser {
	
	
	public DummyRoadUser(){
	}

	@Override
	public void initRoadUser(RoadModel model) {
		//model.addObjectAt(this, point);
	}

}
