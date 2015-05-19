package mas;

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
		if (countDown < EXT_TIME)
			return;
		if (emptyTank) {
			refillStation = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
			roadModel.moveTo(this, refillStation, timeLapse);
		} else {
			if (!roadModel.containsObject(target))
				target = null;
			if (target==null)
				target = RoadModels.findClosestObject(
						roadModel.getPosition(this), roadModel, Fire.class);
			
			if (target != null) 
				roadModel.moveTo(this, target, timeLapse);
			else { 
				// patrouilling
				roadModel.moveTo(this, roadModel.getRandomPosition(rnd), timeLapse);
			}
		}
		
	}

}
