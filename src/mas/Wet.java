package mas;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

public class Wet extends FireStatus {
	private long timeToDry;
	
	public Wet(Point pos, RoadModel model, RandomGenerator rng) {
		super(pos, model, rng);
		timeToDry = WETTIME;
	}

	@Override
	public void tick(TimeLapse time) {
		this.timeToDry -= time.getTimeStep();
		if(this.timeToDry < 0){
			roadModel.removeObject(this);
		}
	}

	@Override
	public void extinguish() {
		this.timeToDry = WETTIME;
	}

}
