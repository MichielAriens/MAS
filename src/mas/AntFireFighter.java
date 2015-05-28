package mas;

import java.util.ArrayList;
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


public class AntFireFighter extends FireFighter{
	
	private AntAI ai;

	public AntFireFighter(PheromoneNode startPosition, LineOfSight los, RandomGenerator rnd) {
		super(startPosition.getPosition(), los, rnd);
		ai = new AntAI(startPosition);
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		//System.out.println(ai.getState());
		
		//Check whether to douse fire.
		Fire closestFire = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, Fire.class);
		RefillStation closestRefill = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
		
		if(!emptyTank && roadModel.equalPosition(this, closestFire)){
			closestFire.extinguish();
			emptyTank = true;
			ai.switchMode();
		}
		
		if(emptyTank && roadModel.equalPosition(this, closestRefill)){
			emptyTank = false;
			ai.switchMode();
		}
		
		if(roadModel.equalPosition(this, ai.getNode())){
			target = ai.nextNode(this);
		}
		roadModel.moveTo(this, target, timeLapse);
	}
	
	@Override
	public void afterTick(TimeLapse timeLapse) {
		
	};
	
	
	public List<Point> getVisibleFire(){
		List<Point> retval = new ArrayList<Point>();
		List<Fire> fires = RoadModels.findClosestObjects(roadModel.getPosition(this), roadModel, Fire.class, 10);
		for(Fire f : fires){
			if(this.los.canSee(this, f)){
				retval.add(f.getPosition());
			}else{
				break;
			}
		}
		return retval;
	}
}
