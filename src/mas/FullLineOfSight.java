package mas;


public class FullLineOfSight extends LineOfSight{

	@Override
	boolean canSee(FireFighter fighter, Fire fire) {
		 return true;
	}

	@Override
	boolean canComm(FireFighter ff1, FireFighter ff2) {
		return true;
	}

	@Override
	double getVisionRadius() {
		return Double.MAX_VALUE;
	}

}
