package mas;


public abstract class LineOfSight {
	
	abstract boolean canSee(FireFighter fighter, Fire fire);
	abstract boolean canComm(FireFighter ff1, FireFighter ff2);
	abstract double getVisionRadius(); // how far can I see

}
