package mas;

import java.util.Collection;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;

public class DumbFireFighter extends FireFighter {

	public DumbFireFighter(Point startPosition, RandomGenerator rnd, LineOfSight los) {
		super(startPosition, rnd, los);
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		if (countDown < EXT_TIME)
			return;
		if (emptyTank) {
			// we assume a firefighter knows the position of all refill stations and doesn't
			// need to 'see' it
			refillStation = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
			roadModel.moveTo(this, refillStation, timeLapse);
		} else {
			if (!roadModel.containsObject(target))
				target = null;
			
			if (target==null) {
				Collection<Fire> closeFire = RoadModels.findObjectsWithinRadius(roadModel.getPosition(this), 
						roadModel, lineOfSight.getVisionRadius(), Fire.class);
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

}
