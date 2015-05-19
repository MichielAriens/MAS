package mas;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class ContractFireFighter extends FireFighter implements CommUser {
	private CommDevice device;
	private final double RANGE = 3;
	private double reliability = 1; // kan mss veranderen door situatie?
	private Set<Fire> tasks;
	// kan rol van manager en contractor hebben
	// hou een lijst bij van plaatsen waar je weet dat vuur is; hiervoor kan je manager zijn
	// task maar aan 1 contractor geven
	// wat als verschillende manager zelfde task hebben -> zo kunnen toch verschillende contractors zelfs task krijgen
	public ContractFireFighter(Point startPosition, RandomGenerator rnd, LineOfSight los) {
		super(startPosition, rnd, los);
		tasks = new HashSet<>();
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		// TODO Auto-generated method stub
		// als die zelf geen target heeft gaat die een task aan zichzelf geven
		// als die al een target heeft komt het in de lijst van tasks waarvoor contractors gezocht worden
		
		// we always look around for fire
		Collection<Fire> closeFire = RoadModels.findObjectsWithinRadius(roadModel.getPosition(this), 
					roadModel, lineOfSight.getVisionRadius(), Fire.class);
		tasks.addAll(closeFire); // no doubles are added
		
		// TODO: problem, when do we know when fire is extinguished
		// (1) we can ask roadmodel, but this would be the same as godview
		// (2) we can 'see' that at a place is no fire anymore
		// (3) the contractor can let us know (if he can reach us)
		
		// TODO : device.broadcast(contents); broadcasts tasks
		
		if (countDown < EXT_TIME) // this means we're extinguishing fire atm
			return;
		
		if (emptyTank) {
			// we assume a firefighter knows the position of all refill stations and doesn't
			// need to 'see' it
			refillStation = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
			roadModel.moveTo(this, refillStation, timeLapse);
		} else {
			if (!roadModel.containsObject(target))
				target = null;
			
			if (target == null) {
				// TODO bit on own / other task lists (?)
			}
			
			// let's move
			if (target != null) 
				roadModel.moveTo(this, target, timeLapse);
			else { 
				// patrouilling TODO less random patrouilling
				roadModel.moveTo(this, roadModel.getRandomPosition(rnd), timeLapse);
			}
		}
	}

	@Override
	public Optional<Point> getPosition() {
		return Optional.of(roadModel.getPosition(this));
	}

	@Override
	public void setCommDevice(CommDeviceBuilder builder) {
		if (RANGE >= 0) {
		      builder.setMaxRange(RANGE);
	    }
	    device = builder.setReliability(reliability).build();
	}

}
