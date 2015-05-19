package mas;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public abstract class FireStatus implements RoadUser {
	
	protected final long WETTIME = 9999999; 
	protected static RoadModel roadModel;
	protected Point position;
	protected RandomGenerator rng;
	
	public FireStatus(Point pos, RoadModel model, RandomGenerator rng) {
		position = pos;
		roadModel = model;
		this.rng = rng;
	}
	/**
	 * One cell's fire status. Returns the next state. 
	 * @param time
	 * @return
	 */
	public abstract void tick(TimeLapse time);
	public abstract void extinguish();
	//public abstract FireStatus ignite();
	public Point getPosition() {return position;}
	
	@Override
	public void initRoadUser(RoadModel model) {
		roadModel = model;
		roadModel.addObjectAt(this, position);
	}
}
	
//	public class Fire extends FireStatus{
//		
//		public Fire(Point pos, RoadModel model) {
//			super(pos, model);
//		}
//
//		@Override
//		public void tick(TimeLapse time) {
//			// do nothing at the moment
//		}
//		
//		@Override
//		public FireStatus extinguish() {
//			roadModel.removeObject(this);
//			System.out.println("Adding new wet at (" + position.x + ", " + position.y + ")");
//			roadModel.addObjectAt(new Wet(position, roadModel), position);
//			//roadModel.addObjectAt(new Fire(position), position);
//			return null;
//		}
//
//		@Override
//		public FireStatus ignite() {
//			// do nothing
//			return null;
//		}
//
//		@Override
//		public void initRoadUser(RoadModel model) {
//			System.out.println("init road user");
//			roadModel = model;
//			roadModel.addObjectAt(this, position);
//		}
//	}
//	
//	public class Wet extends FireStatus{
//		private long timeToDry;
//		
//		public Wet(Point pos, RoadModel model){
//			super(pos, model);
//			this.timeToDry = WETTIME;
//		}
//
//		@Override
//		public void tick(TimeLapse time) {
//			this.timeToDry -= time.getTimeStep();
//			if(this.timeToDry < 0){
//				roadModel.removeObject(this);
//			}
//		}
//
//		@Override
//		public FireStatus extinguish() {
//			this.timeToDry = WETTIME;
//			return this;
//		}
//
//		@Override
//		public FireStatus ignite() {
//			//roadModel.removeObject(this);
//			return this;
//		}
//
//		@Override
//		public void initRoadUser(RoadModel model) {
//			roadModel = model;
//			roadModel.addObjectAt(this, position);
//		}
//	}
	
//	public static class Dry extends FireStatus{
//
//		public Dry(Point pos, RoadModel model) {
//			super(pos, model);
//		}
//
//		@Override
//		public FireStatus tick(TimeLapse time) {
//			roadModel.removeObject(this);
//			return this;
//		}
//
//		@Override
//		public FireStatus extinguish() {
//			roadModel.removeObject(this);
//			return new Wet(position, roadModel);
//		}
//
//		@Override
//		public FireStatus ignite() {
//			roadModel.removeObject(this);
//			return new Fire(position, roadModel);
//		}
//
//		@Override
//		public void initRoadUser(RoadModel model) {
//			roadModel = model;
//			roadModel.addObjectAt(this, position);
//		}
//	}
	
//}
