package fire;

import com.github.rinde.rinsim.core.TimeLapse;

public abstract class FireStatus {
	
	private static final long WETTIME = 10000; 
	
	/**
	 * One cell's fire status. Returns the next state. 
	 * @param time
	 * @return
	 */
	public abstract FireStatus tick(TimeLapse time);
	public abstract FireStatus extinguish();
	public abstract FireStatus ignite();
	
	public class Fire extends FireStatus{
		
		@Override
		public FireStatus tick(TimeLapse time) {
			return this;
		}
		
		@Override
		public FireStatus extinguish() {
			return new Wet();
		}

		@Override
		public FireStatus ignite() {
			return this;
		}
	}
	
	public class Wet extends FireStatus{
		private long timeToDry;
		
		public Wet(){
			this.timeToDry = WETTIME;
		}

		@Override
		public FireStatus tick(TimeLapse time) {
			this.timeToDry -= time.getTime();
			if(this.timeToDry < 0){
				return new Dry();
			}
			return this;
		}

		@Override
		public FireStatus extinguish() {
			this.timeToDry = WETTIME;
			return this;
		}

		@Override
		public FireStatus ignite() {
			return this;
		}
	}
	
	public class Dry extends FireStatus{

		@Override
		public FireStatus tick(TimeLapse time) {
			return this;
		}

		@Override
		public FireStatus extinguish() {
			return new Wet();
		}

		@Override
		public FireStatus ignite() {
			return new Fire();
		}
	}
	
}
