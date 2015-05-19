package mas;

import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class AntFireFighter extends FireFighter{
	
	private enum State{
		LOOKING_FOR_FIRE,
		LOOKING_FOR_WATER,
		FOUNDFIRE,
		FOUNDWATER,
		
	}
	
	private State state;

	public AntFireFighter(Point startPosition, LineOfSight los, RandomGenerator rnd) {
		super(startPosition, los, rnd);
		state = State.LOOKING_FOR_FIRE;
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		if(state == state.LOOKING_FOR_FIRE){
			resolveTargetWandering(timeLapse);
		}
		
		
		move(timeLapse);
	}

	private void move(TimeLapse timeLapse) {
		if(target != null){
			roadModel.moveTo(this, target, timeLapse);
		}
	}

	private void resolveTargetWandering(TimeLapse timeLapse) {
		// TODO Auto-generated method stub
		if(target == null){
			Fire newT = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, Fire.class);
			if(los.canSee(this, newT)){
				target = newT;
			}else{
				target = new DummyRoadUser();
				roadModel.addObjectAt(target, roadModel.getRandomPosition(rnd));
			}
		}
	}


}
