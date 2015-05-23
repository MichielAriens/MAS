package mas;

import java.util.Collection;
import java.util.EmptyStackException;
import java.util.HashSet;
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
	private double reliability = 1; // CHOICE kan dit veranderen?
	private Set<Fire> tasks;
	private long lastAnnouncementTime;
	private List<Message> taskAnnouncements;
	private Stack<Message> awardedContracts;
	private Message targetContract;
	// kan rol van manager en contractor hebben
	// hou een lijst bij van plaatsen waar je weet dat vuur is; hiervoor kan je manager zijn
	// task maar aan 1 contractor geven
	// wat als verschillende manager zelfde task hebben -> zo kunnen toch verschillende contractors zelfs task krijgen
	public ContractFireFighter(Point startPosition, LineOfSight los, RandomGenerator rnd) {
		super(startPosition, los, rnd);
		tasks = new HashSet<>();
		lastAnnouncementTime = -9999999;
		awardedContracts = new Stack<>();
		taskAnnouncements = new LinkedList<>();
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		System.out.println("Tick: " + timeLapse.getStartTime());
		
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
//			device.send(new ContractTaskAnnouncement(taskPoints, lastAnnouncementTime + 100000), this);
		}
		
		// we listen for broadcasts as well
		if (device.getUnreadCount() > 0) {
			List<Message> contractsToPickFrom = new LinkedList<>();
			List<Message> bidsToPickFrom = new LinkedList<>();
			List<Message> messages = device.getUnreadMessages();
			for (Message m : messages) {
				if (target==null && m.getContents().getClass() == ContractTaskAnnouncement.class) {
					if (target == null)
						contractsToPickFrom.add(m);
					else
						taskAnnouncements.add(m);
					// TODO als wel een target gaan we announcements opslaan!
				} else if (m.getContents().getClass() == ContractTaskBid.class) {
					// CHOICE we awarden onmiddellijk om geen tijd te verliezen, we houden dus geen bids bij in de hoop
					// een betere te krijgen
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
			if (target == null) {
				if (!contractsToPickFrom.isEmpty())
					selectBestAndBid(contractsToPickFrom, timeLapse.getEndTime()); // CHOICE bid on only 1 /vs more
				else if (!taskAnnouncements.isEmpty()) { // bidding on old announcements
					selectBestAndBid(taskAnnouncements, timeLapse.getEndTime());
					taskAnnouncements.clear();
				}
			}
				
			
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
				try {
					targetContract = awardedContracts.pop();
					target = ((ContractTaskAward)targetContract.getContents()).task;
				} catch (EmptyStackException ex) {}
			}
			
			// let's move
			if (target != null) {
				// if we can see the target we check if it's still there
				if (los.canSee(this, target)) {
					if (!roadModel.containsObject(target)) {
						
						device.send(new ContractTaskReport((Fire)target, true), targetContract.getSender());
						try {
							targetContract = awardedContracts.pop();
							target = ((ContractTaskAward)targetContract.getContents()).task;
						} catch (EmptyStackException ex) {}
					}
				}
				if (target != null) {
					// we move to the position of the target (possibly target is already extinguished)
					roadModel.moveTo(this, ((Fire)target).position, timeLapse);
				}
			}
			else { 
				// patrolling TODO less random patrolling
				roadModel.moveTo(this, roadModel.getRandomPosition(rnd), timeLapse);
			}
		}
	}
	
	@Override
	public void afterTick(TimeLapse timeLapse) {
//		if (target != null && this.getPosition().equals(((Fire)target).position)) {  dit werkt blijkbaar niet
		if (target != null && !roadModel.containsObject(target)) {
			// it's extinguished!
			device.send(new ContractTaskReport((Fire)target, true), targetContract.getSender());
			target = null;
        	countDown = EXT_TIME;
        	return;
		}
		if (target != null && roadModel.containsObject(target) && roadModel.equalPosition(this, target)) {
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
//		if (RANGE >= 0) {
//		      builder.setMaxRange(RANGE);
//	    }
//	    device = builder.setReliability(reliability).build();
		builder.setMaxRange(los.getCommunicationRadius());
	    device = builder.setReliability(reliability).build();
	}
	
	/**
	 * @pre contracts.empty() == false
	 * @pre MessageContent in contracts is of type ContractTaskAnnouncement
	 * @param contracts
	 */
	private void selectBestAndBid(List<Message> bundledContracts, long time) {
		System.out.println(this + " is going to bid");
		List<Tuple<Point,Message>> bestPerBundle = new LinkedList<>();
		
		for (Message m : bundledContracts) {
			// we only bid if the expirationtime isn't expired
			if (((ContractTaskAnnouncement)m.getContents()).expirationTime < time)
				continue;
			bestPerBundle.add(new Tuple<Point, Message>(selectBestFromContractBundle(m), m));
		}
		
		// TODO sorteren zodat ze in ieder FF hetzelfde staan, als 2 FF's dan op dezelfde bidden
		// komt dit ook bij dezelfde manager aan -> efficienter
		// Denk dat dan alleen nodig hetzelfde punt gekozen kan worden als ze ieder manager van elkaar zijn
		// in de tekst: "to reduce the chance of bidding on the same task with a different manager ..."
//		sort(bestPerBundle);
		//die logica hierboven klopt niet helaas
		
		Tuple<Point, Message> bestContract = bestPerBundle.get(0);
		Point myPosition = roadModel.getPosition(this);
		for (Tuple<Point, Message> t : bestPerBundle) {
			System.out.println("P: " + t.point + "; sender: " + t.message.getSender());
			if (Point.distance(bestContract.point, myPosition) > Point.distance(t.point, myPosition))
				bestContract = t;
		}
		
		System.out.println(this + " is going to bid for " + bestContract.point + " which is a task of " + bestContract.message.getSender());
		
		// bid on best contract
		if (los.canComm(this, (ContractFireFighter)bestContract.message.getSender()))
			device.send(new ContractTaskBid(bestContract.point, myPosition), bestContract.message.getSender());
	}
	
//	private void sort(List<Tuple<Point, Message>> bestPerBundle) {
//		for (int i = 0; i < bestPerBundle.size(); ++i) {
//			for (int j = i+1; j < bestPerBundle.size(); ++j) {
//				System.out.println(bestPerBundle.get(i).message.getSender().toString());
//				if (bestPerBundle.get(i).message.getSender().toString().compareTo(bestPerBundle.get(j).message.getSender().toString()) > 0) {
//					Tuple<Point,Message> tuple = bestPerBundle.get(i);
//					Tuple<Point,Message> tuple2 = bestPerBundle.get(j);
//					bestPerBundle.remove(tuple);
//					bestPerBundle.remove(tuple2);
//					bestPerBundle.add(i, tuple2);
//					bestPerBundle.add(j, tuple);
//				}
//			}
//		}
//	}

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
	
	private void selectBestAndAward(List<Message> bids) {
		PointMessages pm = new PointMessages();
		
		// organise bids per task
		// then get best for each task
		for (Message m : bids) {
			pm.add(new Tuple(((ContractTaskBid)m.getContents()).taskPoint, m));
		}
		
		Set<Point> points = pm.getPoints();
		
		for (Point p : points) {
			List<Message> messages = pm.getAll(p);
			Message bestBidMessage = null;
			ContractTaskBid bestBidContent = null;
			Fire bestTask = null;
			for (Message m : messages) {
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
			// send award to best bid that we still have a task for
			if (bestTask != null && los.canComm(this, (ContractFireFighter)bestBidMessage.getSender())) 
				device.send(new ContractTaskAward(bestTask), bestBidMessage.getSender());
		}
		
		
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
	
	private class PointMessages {
		private List<Tuple<Point, Message>> tuples;
		
		public PointMessages() {tuples = new LinkedList<>();}
		public void add(Tuple<Point, Message> tuple) {
			tuples.add(tuple);
		}
		public List<Message> getAll(Point p) {
			List<Message> messages = new LinkedList<>();
			for (Tuple t : tuples) {
				if (t.point.equals(p))
					messages.add((com.github.rinde.rinsim.core.model.comm.Message) t.message);
			}
			return messages;
		}
		public Set<Point> getPoints() {
			Set<Point> points = new HashSet<>();
			for (Tuple t : tuples)
				points.add((com.github.rinde.rinsim.geom.Point) t.point);
			return points;
		}
	}

	// We don't need explicit contract identifiers in our messages because the contract is
	// identified by the combination of the point of fire and the message sender
	public class ContractTaskAnnouncement implements MessageContents {
		//public final Point elegibilityRange; // don't think this is necessary
		public final Set<Point> taskAbstraction; // the points where the fire is
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
