package mas;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommDevice;
import com.github.rinde.rinsim.core.model.comm.CommDeviceBuilder;
import com.github.rinde.rinsim.core.model.comm.CommUser;
import com.github.rinde.rinsim.core.model.comm.Message;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

public class ContractFireFighter extends FireFighter implements CommUser {
	private CommDevice device;
	private final double RANGE = 3;
	private double reliability = 1; // kan mss veranderen door situatie?
	private Set<Fire> tasks;
	private long lastAnnouncementTime;
	// kan rol van manager en contractor hebben
	// hou een lijst bij van plaatsen waar je weet dat vuur is; hiervoor kan je manager zijn
	// task maar aan 1 contractor geven
	// wat als verschillende manager zelfde task hebben -> zo kunnen toch verschillende contractors zelfs task krijgen
	public ContractFireFighter(Point startPosition, RandomGenerator rnd, LineOfSight los) {
		super(startPosition, los, rnd);
		tasks = new HashSet<>();
		lastAnnouncementTime = -9999999;
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		// TODO
		// als die zelf geen target heeft gaat die een task aan zichzelf geven
		// als die al een target heeft komt het in de lijst van tasks waarvoor contractors gezocht worden
		
		// we always look around for fire
		Collection<Fire> closeFire = RoadModels.findObjectsWithinRadius(roadModel.getPosition(this), 
					roadModel, los.getVisionRadius(), Fire.class);
		boolean hasChanged = tasks.addAll(closeFire); // no doubles are added
		
		// we broadcast if there are new tasks or when we haven't broadcasted in a while (a tick is 10000)
		if (hasChanged || (timeLapse.getStartTime() - lastAnnouncementTime > 100000)) {
			lastAnnouncementTime = timeLapse.getStartTime();
			// doe 1 grote broadcast om communication channel nie te overbelasten
			Set<Point> taskPoints = new HashSet<>();
			for (Fire f : tasks) {
				taskPoints.add(f.getPosition());
			}
			device.broadcast(new ContractTaskAnnouncement(taskPoints, lastAnnouncementTime + 100000));
		}
		
		// we listen for broadcasts as well
		if (device.getUnreadCount() > 0) {
			List<Message> messages = device.getUnreadMessages();
			for (Message m : messages) {
				if (m.getContents().getClass() == ContractTaskAnnouncement.class) {
					// TODO alle contracten bundelen en na deze loop bieden op 1 (of meer ??)
				} else if (m.getContents().getClass() == ContractTaskBid.class) {
					// TODO alle bids bundelen en na deze loop per task de beste kiezen
					// als er eentje goed genoeg is onmiddellijk awarden, anders wachten (tot deadline)
				} else if (m.getContents().getClass() == ContractTaskAward.class) {
					// TODO wat met meerdere awards: opstapelen als tasks of eentje accepten en andere denyen
				}
			}
		}
		
		// TODO: problem, when do we know when fire is extinguished
		// (1) we can ask roadmodel, but this would be the same as godview
		// (2) we can 'see' that at a place is no fire anymore
		// (3) the contractor can let us know (if he can reach us)
		
		// TODO : device.broadcast(contents); broadcasts tasks
		// TODO 1 message voor iedere task of allemaal in 1 shteken?
				// hangt er vanaf of taskabstraction gebruikt wordt om al dan niet te bidden
		
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
				// TODO bid on own / other task lists (?)
				// bid met: huidige positie, target positie, hebEenTarget ?
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
	public void afterTick(TimeLapse timeLapse) {
		if (roadModel.equalPosition(this, target)) {
			--countDown;
			if (countDown == 0) {
				if(target instanceof Fire){
					((Fire) target).extinguish();
					emptyTank = true;
				}
				target = null;
	        	countDown = EXT_TIME;
	        	// TODO broadcast the extinguishing?
			}
        } else if (roadModel.equalPosition(this, refillStation)) {
        	emptyTank = false;
        	refillStation = null;
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
	
	// We don't need explicit contract identifiers in our messages because the contract is
	// identified by the combination of the point of fire and the message sender
	public class ContractTaskAnnouncement implements MessageContents {
		//public final Point elegibilityRange; // don't think this is necessary
		public final Set<Point> taskAbstraction; // the point where the fire is
		//public final Point bidSpecification; // as bid we expect a point, but i don't think it's necessary to express this here
		public final long expirationTime; // deadline for sending bids
		
		public ContractTaskAnnouncement(Set<Point> ta, long et) {
			taskAbstraction = ta;
			expirationTime = et;
		}
	}
	
	public class ContractTaskBid implements MessageContents {
		public final Point taskPoint; // for identifying the contract
		public final Point bidPoint; // the location of the bidder counts as his bid
		
		public ContractTaskBid(Point firePoint, Point fighterLocation) {
			taskPoint = firePoint;
			bidPoint = fighterLocation;
		}
	}
	
	public class ContractTaskAward implements MessageContents {
		public final Fire task;
		
		public ContractTaskAward(Fire fire) {
			task = fire;
		}
	}
}
