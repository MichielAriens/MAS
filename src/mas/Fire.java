package mas;

import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

public class Fire extends FireStatus {
	public Fire(Point pos, RoadModel model, RandomGenerator rnd) {
		super(pos, model, rnd);
	}

	@Override
	public void tick(TimeLapse time) {
		if (rng.nextDouble() < .0115) {
			spread();
		}
	}

	@Override
	public void extinguish() {
		Point p1 = new Point(position.x +1, position.y);
		Point p2 = new Point(position.x -1, position.y);
		Point p3 = new Point(position.x, position.y +1);
		Point p4 = new Point(position.x, position.y -1);
		Set<Fire> firePoints = roadModel.getObjectsOfType(Fire.class);
		for (Fire f : firePoints) {
			if (roadModel.containsObjectAt(f, p1)
					|| roadModel.containsObjectAt(f, p2)
					|| roadModel.containsObjectAt(f, p3)
					|| roadModel.containsObjectAt(f, p4))
				roadModel.removeObject(f);
		}
		
		roadModel.removeObject(this);
		roadModel.addObjectAt(new Wet(position, roadModel, rng), position);
		roadModel.addObjectAt(new Wet(p1, roadModel, rng), p1);
		roadModel.addObjectAt(new Wet(p2, roadModel, rng), p2);
		roadModel.addObjectAt(new Wet(p3, roadModel, rng), p3);
		roadModel.addObjectAt(new Wet(p4, roadModel, rng), p4);
		//TODO: catch placing objects outside the boundries
	}
	
	private void spread() {
		Point p = getSpreading(position);
		Set<FireStatus> takenPoints = roadModel.getObjectsOfType(FireStatus.class);
		
		boolean empty = true;
		for (FireStatus f : takenPoints) {
			if (roadModel.containsObjectAt(f, p)) {
				empty = false;
				break;
			}
		}
		if (empty && isPointInBoundary(p)) {
			roadModel.addObjectAt(new Fire(p, roadModel, rng), p);
		}
	}
	
	private Point getSpreading(Point p) {
		double d = rng.nextDouble();
    	if (d < 0.25)
    		return new Point(p.x, p.y +1);
    	if (d < 0.5)
    		return new Point(p.x, p.y -1);
    	if (d < 0.75)
    		return new Point(p.x +1, p.y +1);
    	else
    		return new Point(p.x -1, p.y +1);
    }
	
	private boolean isPointInBoundary(Point p) {
		List<Point> bounds = roadModel.getBounds();
		Point minp = bounds.get(0);
		Point maxp = bounds.get(1);
        return p.x > minp.x && p.x < maxp.x && p.y > minp.y && p.y < maxp.y;
    }
}
