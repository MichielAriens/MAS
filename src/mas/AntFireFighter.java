package mas;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class AntFireFighter extends FireFighter{
	
	private Point returnTo = null;
	private AntPheromone lastPlacedPheromone = null;
	
	
	private enum State{
		LOOKING_FOR_FIRE,
		LOOKING_FOR_WATER,
		FOUND_FIRE,
		FOUND_WATER,
		TRACING, 
	}
	
	private State state;

	public AntFireFighter(Point startPosition, LineOfSight los, RandomGenerator rnd) {
		super(startPosition, los, rnd);
		state = State.LOOKING_FOR_FIRE;
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		checkState(timeLapse);
		//System.out.println(state);
		
		if(state == state.LOOKING_FOR_FIRE || state == state.FOUND_FIRE){
			resolveTargetWandering(timeLapse);
		}else if(state == state.LOOKING_FOR_WATER || state == state.FOUND_WATER){
			resolveTargetLookingForWater(timeLapse);
		}else if(state == )
		
		
		move(timeLapse);
	}

	private void checkState(TimeLapse timeLapse) {
		if(emptyTank){
			if(target == null || target instanceof DummyRoadUser){
				state = State.LOOKING_FOR_WATER;
			}else{
				state = State.FOUND_WATER;
			}
		}else{
			//if(returnTo != null){
			//	state = State.TRACING;
			//}
			else if(target == null || target instanceof DummyRoadUser){
				state = State.LOOKING_FOR_FIRE;
			}else{
				state = State.FOUND_FIRE;
			}
		}
	}

	private void move(TimeLapse timeLapse) {
		if(returnTo != null){
			roadModel.moveTo(this, returnTo, timeLapse);
		}
		if(target != null){
			if(roadModel.containsObject(target)){
				roadModel.moveTo(this, roadModel.getPosition(target), timeLapse);
			}else{
				target = null;
			}
		}
	}

	private void resolveTargetWandering(TimeLapse timeLapse) {
		// First check for nearer fires.
		Fire newT = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, Fire.class);
		if(los.canSee(this, newT)){
			if(target instanceof DummyRoadUser){
				roadModel.removeObject(target);
			}
			target = newT;
		}
		if(target == null){
			target = new DummyRoadUser();
			roadModel.addObjectAt(target, roadModel.getRandomPosition(rnd));
		}
	}
	
	private void resolveTargetLookingForWater(TimeLapse timeLapse) {
		// First check for nearer fires.
		RefillStation newT = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
		if(los.canSee(this, newT)){
			if(target instanceof DummyRoadUser){
				roadModel.removeObject(target);
			}
			target = newT;
			refillStation = newT;
		}
		if(target == null){
			target = new DummyRoadUser();
			roadModel.addObjectAt(target, roadModel.getRandomPosition(rnd));
		}
	}
	
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


}
