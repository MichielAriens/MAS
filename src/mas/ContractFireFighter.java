package mas;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

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
	private Stack<Message> awardedContracts;
	private Message targetContract;
	// kan rol van manager en contractor hebben
	// hou een lijst bij van plaatsen waar je weet dat vuur is; hiervoor kan je manager zijn
	// task maar aan 1 contractor geven
	// wat als verschillende manager zelfde task hebben -> zo kunnen toch verschillende contractors zelfs task krijgen
	public ContractFireFighter(Point startPosition, RandomGenerator rnd, LineOfSight los) {
		super(startPosition, los, rnd);
		tasks = new HashSet<>();
		lastAnnouncementTime = -9999999;
		awardedContracts = new Stack<>();
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
			// CHOICE doe 1 grote broadcast om communication channel nie te overbelasten
			Set<Point> taskPoints = new HashSet<>();
			for (Fire f : tasks) {
				taskPoints.add(f.getPosition());
			}
			device.broadcast(new ContractTaskAnnouncement(taskPoints, lastAnnouncementTime + 100000));
		}
		
		// we listen for broadcasts as well
		if (device.getUnreadCount() > 0) {
			List<Message> contractsToPickFrom = new LinkedList<>();
			List<Message> bidsToPickFrom = new LinkedList<>();
			List<Message> messages = device.getUnreadMessages();
			for (Message m : messages) {
				if (target==null && m.getContents().getClass() == ContractTaskAnnouncement.class) {
					contractsToPickFrom.add(m);
				} else if (m.getContents().getClass() == ContractTaskBid.class) {
					// TODO als er eentje goed genoeg is onmiddellijk awarden, anders wachten (tot deadline)
					// TODO dus alle bids bijhouden zolang je niet ge-award hebt
					bidsToPickFrom.add(m);
				} else if (m.getContents().getClass() == ContractTaskAward.class) {
					// CHOICE queue awarded contracts /vs accept one
					awardedContracts.push(m);
				} else if (m.getContents().getClass() == ContractTaskReport.class) {
					ContractTaskReport report = (ContractTaskReport) m.getContents();
					if (report.extinguished)
						tasks.remove(report.task);
				}
			}
			if (!contractsToPickFrom.isEmpty())
				selectBestAndBid(contractsToPickFrom); // CHOICE bid on only 1 /vs more
			
			if (!bidsToPickFrom.isEmpty())
				selectBestAndAward(bidsToPickFrom);
			
		}
		
		// CHOICE problem, when do we know when fire is extinguished
		// (1) we can ask roadmodel, but this would be the same as godview
		// (2) we can 'see' that at a place is no fire anymore
		// (3) the contractor can let us know (if he can reach us)
		// CHOICE now is option (3)
		
		
		if (countDown < EXT_TIME) // this means we're extinguishing fire atm
			return;
		
		if (emptyTank) {
			// we assume a firefighter knows the position of all refill stations and doesn't
			// need to 'see' it
			refillStation = RoadModels.findClosestObject(roadModel.getPosition(this), roadModel, RefillStation.class);
			roadModel.moveTo(this, refillStation, timeLapse);
		} else {
			if (target == null) {
				targetContract = awardedContracts.pop();
				target = ((ContractTaskAward)targetContract.getContents()).task;
				// TODO bid on own / other task lists (?)
				// bid met: huidige positie, target positie, TODO hebEenTarget ?
			}
			
			// let's move
			if (target != null) {
				// if we can see the target we check if it's still there
				if (los.canSee(this, target)) {
					if (!roadModel.containsObject(target)) {
						device.send(new ContractTaskReport((Fire)target, true), targetContract.getSender());
						targetContract = awardedContracts.pop();
						target = ((ContractTaskAward)targetContract.getContents()).task;
					}
				} else {
					// we move to the position of the target (possibly target is already extinguished)
					roadModel.moveTo(this, ((Fire)target).position, timeLapse);
				}
			}
			else { 
				// patrouilling TODO less random patrouilling
				roadModel.moveTo(this, roadModel.getRandomPosition(rnd), timeLapse);
			}
		}
	}
	
	@Override
	public void afterTick(TimeLapse timeLapse) {
		if (this.getPosition().equals(((Fire)target).position)) {
			--countDown;
			if (countDown == 0) {
				if(target instanceof Fire){
					((Fire) target).extinguish();
					emptyTank = true;
				}
				device.send(new ContractTaskReport((Fire)target, true), targetContract.getSender());
				target = null;
	        	countDown = EXT_TIME;
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
	
	/**
	 * @pre contracts.empty() == false
	 * @pre MessageContent in contracts is of type ContractTaskAnnouncement
	 * @param contracts
	 */
	private void selectBestAndBid(List<Message> bundledContracts) {
		List<Tuple<Point,Message>> bestPerBundle = new LinkedList<>();
		
		for (Message m : bundledContracts) {
			bestPerBundle.add(new Tuple<Point, Message>(selectBestFromContractBundle(m), m));
		}
		
		Tuple<Point, Message> bestContract = bestPerBundle.get(0);
		Point myPosition = roadModel.getPosition(this);
		for (Tuple<Point, Message> t : bestPerBundle) {
			if (Point.distance(bestContract.point, myPosition) > Point.distance(t.point, myPosition))
				bestContract = t;
		}
		
		// bid on best contract
		// TODO can comm?
		device.send(new ContractTaskBid(bestContract.point, myPosition), bestContract.message.getSender());
	}
	
	private Point selectBestFromContractBundle(Message m) {
		Set<Point> taskPoints = ((ContractTaskAnnouncement)m.getContents()).taskAbstraction;
		Point bestPoint = null;
		Point myPosition = roadModel.getPosition(this);
		
		for (Point p : taskPoints) {
			if (bestPoint == null) {
				bestPoint = p;
				continue;
			}
			if (Point.distance(bestPoint, myPosition) > Point.distance(p, myPosition))
				bestPoint = p;
		}
		return bestPoint;
	}
	
	/**
	 * @pre bids.empty() == false
	 * @param bids
	 */
	
	// TODO lijst van bids kan voor verschillende tasks zijn, nu award die slechts 1 task
	private void selectBestAndAward(List<Message> bids) {
		Message bestBidMessage = null;
		ContractTaskBid bestBidContent = null;
		Fire bestTask = null;
		
		// TODO organise bids per task
		// then get best for each task and see if it suffices
		
		for (Message m : bids) {
			ContractTaskBid b = (ContractTaskBid) m.getContents();
			Fire f;
			// check if we still offer the task
			if ((f = taskAvailable(b.taskPoint)) == null)
				continue;
			
			if (bestTask == null) {
				bestTask = f;
				bestBidContent = b;
				bestBidMessage = m;
				continue;
			}
			
			if (Point.distance(b.taskPoint, b.bidPoint) < Point.distance(bestBidContent.taskPoint, bestBidContent.bidPoint)) {
				bestBidMessage = m;
				bestBidContent = b;
				bestTask = f;
			}
		}
		
		// send award to best bid that we still have a task for TODO can comm?
		device.send(new ContractTaskAward(bestTask), bestBidMessage.getSender());
	}
	
	private Fire taskAvailable(Point p) {
		for (Fire f : tasks) {
			if (f.position.equals(p))
				return f;
		}
		return null;
	}
	
	private class Tuple<Point, Message> { 
		  public final Point point; 
		  public final Message message; 
		  public Tuple(Point x, Message y) { 
		    this.point = x; 
		    this.message = y; 
		  } 
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
	
	public class ContractTaskReport implements MessageContents {
		public final Fire task;
		public final boolean extinguished;
		
		public ContractTaskReport(Fire fire, boolean ext) {
			task = fire;
			extinguished = ext;
		}
	}
}
