package mas;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

public class ContractFireFighter extends FireFighter {
	// kan rol van manager en contractor hebben
	// hou een lijst bij van plaatsen waar je weet dat vuur is; hiervoor kan je manager zijn
	public ContractFireFighter(Point startPosition, RandomGenerator rnd) {
		super(startPosition, rnd);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		// TODO Auto-generated method stub
		
	}

}
