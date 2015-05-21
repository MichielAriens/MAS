package mas;

import java.util.Collection;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class DumbFireFighter extends FireFighter {
	
	
	public DumbFireFighter(Point startPosition, LineOfSight los, RandomGenerator rnd) {
		super(startPosition, los, rnd);

	}

	@Override
	public void tick(TimeLapse timeLapse) {
		if (countDown < EXT_TIME) // this means we're extinguishing fire atm
			return;
		if (emptyTank) {
			// we assume a firefighter knows the position of all refill stations and doesn't
			// need to 'see' it
			refillStation = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
			roadModel.moveTo(this, refillStation, timeLapse);
		} else {
			if (!roadModel.containsObject(target))
				target = null;

//			if (target==null)
//				target = RoadModels.findClosestObject(
//						roadModel.getPosition(this), roadModel, Fire.class);

			
			if (target==null) {
				Collection<Fire> closeFire = RoadModels.findObjectsWithinRadius(roadModel.getPosition(this), 
						roadModel, los.getVisionRadius(), Fire.class);
				target = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, closeFire);
			}

			
			if (target != null) 
				roadModel.moveTo(this, target, timeLapse);
			else { 
				// patrouilling TODO less random patrouilling
				roadModel.moveTo(this, roadModel.getRandomPosition(rnd), timeLapse);
			}
		}
		
	}
	
	@Override
	public void afterTick(TimeLapse timeLapse) {
		if (roadModel.equalPosition(this, target)) {
			--countDown;
			if (countDown == 0) {
				if(target instanceof Fire){
					((Fire) target).extinguish();
					emptyTank = true;
				}
	        	target = null;
	        	countDown = EXT_TIME;
			}
        } else if (roadModel.equalPosition(this, refillStation)) {
        	emptyTank = false;
        	refillStation = null;
        }
		
	}

}
