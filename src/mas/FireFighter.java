package mas;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public abstract class FireFighter implements MovingRoadUser, TickListener {
	protected static final double SPEED = 20d;
	protected RoadUser target; // the next target to extinguish
	protected RefillStation refillStation;
	protected RoadModel roadModel;
	protected Point startPosition;
	protected RandomGenerator rnd;
	protected boolean emptyTank;
	protected int countDown;
	protected final int EXT_TIME = 30;

	protected LineOfSight los;
	
	public FireFighter(Point startPosition, LineOfSight los, RandomGenerator rnd) {
		this.startPosition = startPosition;
		target = null;
		this.rnd = rnd;
		emptyTank = false;
		countDown = EXT_TIME;
		this.los = los;
	}
	
	@Override
	public double getSpeed() {
		return SPEED;
	}

	@Override
	public void initRoadUser(RoadModel model) {
		roadModel = model;
		roadModel.addObjectAt(this, startPosition);
	}

//	@Override
//	public void tick(TimeLapse timeLapse) {
//		if (countDown < EXT_TIME)
//			return;
//		if (emptyTank) {
//			refillStation = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
//			roadModel.moveTo(this, refillStation, timeLapse);
//		} else {
//			if (!roadModel.containsObject(target))
//				target = null;
//			
//			if (target==null)
//				target = RoadModels.findClosestObject(
//				          roadModel.getPosition(this), roadModel, Fire.class);
//			
//			if (target != null) 
//				roadModel.moveTo(this, target, timeLapse);
//			else { 
//				// patrouilling
//				roadModel.moveTo(this, roadModel.getRandomPosition(rnd), timeLapse);
//			}
//		}
//		
//	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		if (roadModel.equalPosition(this, target)) {
			--countDown;
			if (countDown == 0) {
				if(target instanceof RefillStation){
					emptyTank = false;
				}
				if(target instanceof DummyRoadUser){
					roadModel.removeObject(target);
				}
				if(target instanceof Fire){
					((Fire)target).extinguish();
					emptyTank = true;
				}
	        	target = null;
	        	countDown = EXT_TIME;
			}
        } 
		
//		else if (roadModel.equalPosition(this, refillStation)) {
//        	emptyTank = false;
//        	refillStation = null;
//        }
		
	}


}
