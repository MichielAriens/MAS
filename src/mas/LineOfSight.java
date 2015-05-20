package mas;


public abstract class LineOfSight {
	
	abstract boolean canSee(FireFighter fighter, Fire fire);
	abstract boolean canComm(FireFighter ff1, FireFighter ff2);
	abstract double getVisionRadius(); // how far can I see

	// TODO: ik denk dat contract net het beste werkt tov andere in de situatie
	// dat canSee beperkt is, maar canComm niet (dus communicatie altijd mogelijk, maar geen weet van al het vuur)
}
