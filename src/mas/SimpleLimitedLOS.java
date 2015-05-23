package mas;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class SimpleLimitedLOS extends LineOfSight{
	
	private float viewDist; 
	private float commDist;
	private RoadModel roadModel;
	
	public SimpleLimitedLOS(float viewDist, float commDist, RoadModel rm){
		this.viewDist = viewDist;
		this.commDist = commDist;
		this.roadModel = rm;
	}
	
	@Override
	boolean canComm(RoadUser u1, RoadUser u2) {
		if(u1 == null || u2 == null){
			return false;
		}
		if(Point.distance(roadModel.getPosition(u1), roadModel.getPosition(u2)) <= commDist){
			return true;
		}return false;
	}
	
	@Override
	boolean canSee(RoadUser u1, RoadUser u2) {
		if(u1 == null || u2 == null){
			return false;
		}
		if(Point.distance(roadModel.getPosition(u1), roadModel.getPosition(u2)) <= viewDist){
			return true;
		}return false;
	}

	@Override
	double getVisionRadius() {
		return viewDist;
	}

	@Override
	double getCommunicationRadius() {
		return commDist;
	}

}
