package mas;

import com.github.rinde.rinsim.core.model.road.RoadUser;


public abstract class LineOfSight {
	

	abstract boolean canSee(RoadUser u1, RoadUser u2);
	abstract boolean canComm(RoadUser u1, RoadUser u2);

	abstract double getVisionRadius(); // how far can I see

	// TODO: ik denk dat contract net het beste werkt tov andere in de situatie
	// dat canSee beperkt is, maar canComm niet (dus communicatie altijd mogelijk, maar geen weet van al het vuur)
}
