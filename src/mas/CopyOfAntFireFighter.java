package mas;

import java.util.List;

import org.apache.commons.math3.analysis.integration.RombergIntegrator;
import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
/**
 * pre
 * @author Michiel
 *
 */


public class CopyOfAntFireFighter extends FireFighter{
	
	private static final float PHEROMONE_DISTANCE = 1;
	
	private DummyRoadUser returnTo = null;
	private AntPheromone lastPlacedPheromone = null;
	
	private AntPheromone lastFollowedPheromene = null;
	private boolean gaveUpOnPheromone = false;
	private boolean followingPath = false;
	
	
	private enum State{
		LOOKING_FOR_FIRE,
		LOOKING_FOR_WATER,
		FOUND_FIRE,
		FOUND_WATER,
		TRACING, 
	}
	
	private State state;

	public CopyOfAntFireFighter(Point startPosition, LineOfSight los, RandomGenerator rnd) {
		super(startPosition, los, rnd);
		state = State.LOOKING_FOR_FIRE;
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		checkState(timeLapse);
		System.out.println(state);
		System.out.println(gaveUpOnPheromone);
		
		if(state == state.LOOKING_FOR_FIRE || state == state.FOUND_FIRE){
			resolveTargetWandering(timeLapse);
		}else if(state == state.LOOKING_FOR_WATER || state == state.FOUND_WATER){
			resolveTargetLookingForWater(timeLapse);
		}else if(state == state.TRACING){
			traceBack(timeLapse);
		}
		
		
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
			if(returnTo != null){
				state = State.TRACING;
			}else if(target == null || target instanceof DummyRoadUser){
				state = State.LOOKING_FOR_FIRE;
			}else{
				state = State.FOUND_FIRE;
			}
		}
	}

	private void move(TimeLapse timeLapse) {
		if(returnTo != null && state == State.TRACING){
			if(roadModel.containsObject(returnTo)){
				roadModel.moveTo(this, returnTo, timeLapse);
			}else{
				returnTo = null;
			}
			
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
		
		//if nothing found try using pheromones
		if(target == null && !gaveUpOnPheromone){
			AntPheromone pherom = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, AntPheromone.class);
			if(pherom != null && this.los.canSee(this, pherom)){
				if(!followingPath){
					target = pherom;
					followingPath = true;
				}else{
					AntPheromone nextT = pherom;
					while(nextT != null && this.los.canSee(this, nextT)){
						nextT = nextT.getAChild();
					}
					if(target == null){
						followingPath = false;
						gaveUpOnPheromone = true;
					}
					target = nextT;
				}
			}
		}
		
		//if still nothing randomly wander.
		if(target == null){
			target = new DummyRoadUser();
			roadModel.addObjectAt(target, roadModel.getRandomPosition(rnd));
		}
	}
	
	private void resolveTargetLookingForWater(TimeLapse timeLapse) {
		// First check for nearer refill stations.
		RefillStation newT = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
		if(los.canSee(this, newT)){
			if(target instanceof DummyRoadUser){
				roadModel.removeObject(target);
			}
			target = newT;
			refillStation = newT;
		}
		
		//if nothing found try using pheromones
		if(target == null && !gaveUpOnPheromone){
			AntPheromone pherom = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, AntPheromone.class);
			if(pherom != null && this.los.canSee(this, pherom)){
				if(!followingPath){
					target = pherom;
					followingPath = true;
				}else{
					AntPheromone nextT = pherom;
					while(nextT != null && this.los.canSee(this, nextT)){
						nextT = nextT.getParent();
					}
					if(target == null){
						followingPath = false;
						gaveUpOnPheromone = true;
					}
					target = nextT;
				}
			}
		}
		
		//if nothing found just wander
		if(target == null){
			target = new DummyRoadUser();
			roadModel.addObjectAt(target, roadModel.getRandomPosition(rnd));
		}
	}
	
	
	private void traceBack(TimeLapse timeLapse) {
		if(roadModel.equalPosition(this, returnTo)){
			returnTo = null;
		}
		Point pos = roadModel.getPosition(this);
		if(lastPlacedPheromone == null 
				|| 
				(roadModel.containsObject(lastPlacedPheromone)
						&& Point.distance(pos, roadModel.getPosition(this.lastPlacedPheromone)) >= PHEROMONE_DISTANCE)){
			AntPheromone nextPherom = new AntPheromone(pos, rnd);
			if(lastFollowedPheromene != null){
				lastPlacedPheromone.addChild(nextPherom);
			}
			lastPlacedPheromone = nextPherom;
			roadModel.register(lastPlacedPheromone);
		}
		target = returnTo;
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
		if (roadModel.equalPosition(this, target)) {
			--countDown;
			//todo countdown removed.
			countDown = 0;
			if (countDown == 0) {
				if(target instanceof RefillStation){
					emptyTank = false;
					gaveUpOnPheromone = false;
				}
				if(target instanceof DummyRoadUser){
					roadModel.removeObject(target);
				}
				if(target instanceof Fire){
					returnTo = new DummyRoadUser();
					roadModel.addObjectAtSamePosition(returnTo, target);
					((Fire)target).extinguish();
					emptyTank = true;
					lastPlacedPheromone = null;
					gaveUpOnPheromone = false;
				}
	        	target = null;
	        	countDown = EXT_TIME;
			}
        } 
	}

}
