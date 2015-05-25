package mas;

import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.graphics.RGB;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.TickListener;
import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.road.PlaneRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;

public class Main {
	/**
	 * ---> x
	 * |
	 * v y
	 */
	static Point MIN_POINT; // lefttop
	static Point MAX_POINT; // rightbottom
	private static RoadModel roadModel;
	
	public Main(Point minp, Point maxp) {
		MIN_POINT = minp;
		MAX_POINT = maxp;
	}
	
	/**
	 * Starts the example.
	 * @param args This is ignored.
	 */
	public static void main(String[] args) {
		MIN_POINT = new Point(0,0);//minp;
		MAX_POINT = new Point(20,12);//maxp;

		// first arg: 0 for dumb fire fighters
		//run(0,1,123L);

		run(1,3,2,3);
		//run(2,1,2,2);

		
//		// TODO voor simulaties:
//		for (int i = 0; i < 50000; ++i) {
//			// dit test 3 communicatiemodellen in zelfde situatie:
//			run(0,1,i);
//			run(1,1,i);
//			run(2,1,i);
//			
//			// dit doet dat ook maar dan met 2 fire fighters
//			run(0,2,i);
//			run(1,2,i);
//			run(2,2,i);
//		}
//		// TODO uiteraard nog resultaten bijhouden en GUI weglaten voor simulaties
	}

	public static void run(int modus, int numFireFighters, int numFires, long seed) {
		roadModel = PlaneRoadModel.builder()
		        .setMinPoint(MIN_POINT)
		        .setMaxPoint(MAX_POINT)
		        .build();
	    // initialize a new Simulator instance
	    final Simulator sim = Simulator.builder()
	        // set the length of a simulation 'tick'
	        .setTickLength(10000L)
	        // set the random seed we use in this 'experiment'
	        // -> Sequences of values generated starting with the same seeds should be identical.
	        // => TODO seed gebruiken om zelfde situatie voor verschillende communicatiemodellen te evalueren
	        .setRandomSeed(seed)
	        // add a PlaneRoadModel, a model which facilitates the moving of
	        // RoadUsers on a plane.
	        .addModel(roadModel)
	        .addModel(CommModel.builder().build()) // TODO mag dit altijd ?
	        .build();
	    
	    final RandomGenerator rng = sim.getRandomGenerator();
	    final RandomGenerator agvRng = new MersenneTwister(123); 
	    // the second generator is used by agv's, 
	    //we need a different one because we don't want the type of agv 
	    //and its usage of the random generator to interfere with the behaviour of the environment (eg spreading fire)
	    
	    // fire
	    for(int i = 0; i < numFires; i++){
	    	Point p;
	    	do {
		    	p = roadModel.getRandomPosition(rng);
		    	
		    	System.out.println(p);
		    } while (!isPointInBoundary(p)); // boundaries can't get on fire
		    p = new Point((int)p.x, (int)p.y);
		    roadModel.addObjectAt(new Fire(p, roadModel, rng), p);
	    }
	    
	    // fire fighters
	    if (modus == 0) {
		    for (int i = 0; i < numFireFighters; i++) {

		    	sim.register(new DumbFireFighter(roadModel.getRandomPosition(rng), new FullLineOfSight(), agvRng));

		    }
	    } else if (modus == 1) {
	    	for (int i = 0; i < numFireFighters; i++) {

		    	sim.register(new ContractFireFighter(roadModel.getRandomPosition(rng), new FullLineOfSight(), agvRng));

		    }
	    } else if (modus == 2) {
	    	for(int i = 0; i < numFireFighters; i++){

	    		//sim.register(new AntFireFighter(roadModel.getRandomPosition(rng), new SimpleLimitedLOS(5,5,roadModel), rng));
	    		sim.register(new AntFireFighter(roadModel.getRandomPosition(rng), new SimpleLimitedLOS(10,10, roadModel), rng));	    	

	    	} 
	    }
	    
	    // refill stations
	    //sim.register(new RefillStation(new Point(7,0)));
	    sim.register(new RefillStation(new Point(20, 7)));
	    
	    sim.addTickListener(new TickListener() {
	        @Override
	        public void tick(TimeLapse time) {
	        	// Fire fighting successful
	        	if (roadModel.getObjectsOfType(Fire.class).isEmpty()) {
	        		// end simulation
	        		System.out.println(sim.getCurrentTime());
	        		sim.stop();
	        	}
	        	
	        	// TODO uitzichtloze situatie stopzetten (bij groot percentage cellen on fire)
	        	
	        	
	        	Set<FireStatus> cells = roadModel.getObjectsOfType(FireStatus.class);
	        	for (FireStatus f : cells)
	        		f.tick(time);
	        	Set<AntPheromone> pheroms = roadModel.getObjectsOfType(AntPheromone.class);
	        	for(AntPheromone f : pheroms){
	        		f.tick(time);
	        	}
	        }

	        @Override
	        public void afterTick(TimeLapse time) {
	        	Set<AntPheromone> pheroms = roadModel.getObjectsOfType(AntPheromone.class);
	        	for(AntPheromone f : pheroms){
	        		f.afterTick(time);
	        	}
	        }
	      });
	    
	    final View.Builder viewBuilder = View.create(sim)
	            .with(PlaneRoadModelRenderer.create())
	            .with(RoadUserRenderer.builder()
	            	   .addImageAssociation(
	            	                Fire.class, "/graphics/perspective/tall-building-64.png")
	            			   		//Fire.class, "img/fire.png")
	            			   		
	            	   .addImageAssociation(
	    	            	        Wet.class, "/graphics/flat/person-red-32.png")
	            			   		//Wet.class, "img/wet.png"));
	    	           .addImageAssociation(
	    	            	        AntFireFighter.class, "/graphics/flat/bus-32.png")
	    	           .addColorAssociation(DummyRoadUser.class, new RGB(0, 0, 0))
	    	           .addColorAssociation(AntPheromone.class, new RGB(0, 255, 0))
	    	     );      
	    viewBuilder.show();
	    // in case a GUI is not desired, the simulation can simply be run by
	    // calling the start method of the simulator.
	}
	
	
	
	private static boolean isPointInBoundary(Point p) {
		return p.x > MIN_POINT.x+0.5 && p.x < MAX_POINT.x-0.5 && p.y > MIN_POINT.y+0.5 && p.y < MAX_POINT.y-0.5;
    }
}
