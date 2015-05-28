package mas;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;

public class Experiments {
	/**
	 * ---> x
	 * |
	 * v y
	 */
	static Point MIN_POINT; // lefttop
	static Point MAX_POINT; // rightbottom
	private static RoadModel roadModel;
	private static int failureThreshold;
	private static int modus;
	private static int numFireFighters;
	private static int numFires;
	private static long seed;
	private static PrintWriter writer;
	private static int losSight;
	private static int losComm;
	
	public Experiments(Point minp, Point maxp) {
		MIN_POINT = minp;
		MAX_POINT = maxp;
	}
	
	/**
	 * Starts the example.
	 * @param args This is ignored.
	 */
	public static void main(String[] args) {
		MIN_POINT = new Point(0,0);//minp;
		MAX_POINT = new Point(20,17);//maxp;
		failureThreshold = (int) (((MAX_POINT.x - MIN_POINT.x) * (MAX_POINT.y - MIN_POINT.y)) / 1.2);

		// first arg: 0 for dumb fire fighters
		//run(0,1,123L);


		//run(1,50,2,3,3,2,true);
		
		
		
		
		
		//run(2,1,2,2);


		//run(0,2,2,1);
//		run(0,1,2,8);
		
		try {
			writer = new PrintWriter("comp1.csv", "UTF-8");
			writer.println("speed=" + FireFighter.SPEED);
			writer.println("extinguishingtime=" + FireFighter.EXT_TIME);
			writer.println("firespreadchance=" + Fire.FIRE_SPREAD_CHANCE);
			writer.println("modus0=DumbFireFighter");
			writer.println("modus1=ContractFireFighter");
			writer.println("lineofsight=variable");
			writer.println("modus,numFireFighters,numFires,seed,losSight,losComm,amountOfTicks");
			writer.println();
			
			int maxSeed = 10;
			
			for(int los = 10; los >=3; los--){
				for(int mode = 0; mode < 3; mode++){
					writer.print(mode + "," + los);
					for(int seed = 0; seed < maxSeed; seed++){
						System.out.println("los:" + los + " mode: " +  mode + " seed: " + seed);
						run(mode,50,2,los,los,seed, false);
						writer.flush();
					}
					writer.println();
				}
			}

			
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (Exception ex) {
			ex.printStackTrace();
			writer.write(ex.getMessage());
		} finally {
			writer.close();
		}
		// TODO resultaten bijhouden
		// TODO LOS ook als variabele
	}

	public static void run(int modus, int numFireFighters, int numFires, int losSight, int losComm, long seed, boolean gui) {
		Experiments.modus = modus;
		Experiments.numFireFighters = numFireFighters;
		Experiments.numFires = numFires;
		Experiments.seed = seed;
		Experiments.losSight = losSight;
		Experiments.losComm = losComm;
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
	        .setRandomSeed(seed)
	        // add a PlaneRoadModel, a model which facilitates the moving of
	        // RoadUsers on a plane.
	        .addModel(roadModel)
	        .addModel(CommModel.builder().build())
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
		    	
//		    	System.out.println(p);
		    } while (!isPointInBoundary(p)); // boundaries can't get on fire
		    p = new Point(Math.round(p.x), Math.round(p.y));
		    roadModel.addObjectAt(new Fire(p, roadModel, rng), p);
	    }
	    
	    
	    
	    // refill stations
	    //sim.register(new RefillStation(new Point(7,0)));
	    Point refilPoint = new Point(20,7);
	    sim.register(new RefillStation(refilPoint));
	    
	    // fire fighters
	    if (modus == 0) {
		    for (int i = 0; i < numFireFighters; i++) {

		    	sim.register(new SimpleFireFighter(refilPoint, new SimpleLimitedLOS(losSight, losComm, roadModel), agvRng));

		    }
	    } else if (modus == 1) {
	    	for (int i = 0; i < numFireFighters; i++) {

		    	sim.register(new ContractFireFighter(refilPoint, new SimpleLimitedLOS(losSight, losComm, roadModel), agvRng));

		    }
	    } else if (modus == 2) {
	    	//Build the grid
	    	for(int x = 0; x <= MAX_POINT.x; x++){
    			for(int y = 0; y <= MAX_POINT.y; y++){
    				sim.register(new PheromoneNode(new Point(x, y), rng));
    			}
    		}
	    	for(PheromoneNode n : roadModel.getObjectsOfType(PheromoneNode.class)){
	    		for(PheromoneNode other : RoadModels.findClosestObjects(n.getPosition(), roadModel, PheromoneNode.class, 16)){
	    			if(!(n.equals(other)) && Point.distance(n.getPosition(), other.getPosition()) <= Math.sqrt(2) + 0.001 ){
	    				n.addEdge(new PheromoneEdge(n, other));
	    			}
	    		}
	    	}
	    	
	    	
	    
	    	for(int i = 0; i < numFireFighters; i++){
	    		PheromoneNode start = RoadModels.findClosestObject(refilPoint, roadModel, PheromoneNode.class);
	    		sim.register(new AntFireFighter(start, new SimpleLimitedLOS(losSight, losComm, roadModel), rng));	    	

	    	} 
	    }
	    

	    // refill stations
	    //sim.register(new RefillStation(new Point(7,0)));
	    //sim.register(new RefillStation(new Point(20, 7)));
	    
	    sim.addTickListener(new TickListener() {
	        @Override
	        public void tick(TimeLapse time) {
	        	// Fire fighting successful
	        	if (roadModel.getObjectsOfType(Fire.class).isEmpty()) {
	        		// end simulation
	        		//writer.println(Experiments.modus + "," + Experiments.numFireFighters + "," + Experiments.numFires
	        		//		+ "," + Experiments.seed + "," + Experiments.losSight + "," + Experiments.losComm + "," + sim.getCurrentTime()/10000);
	        		writer.print("," + sim.getCurrentTime()/10000);
	        		sim.stop();
	        	}
	        	
	        	if (roadModel.getObjectsOfType(Fire.class).size() > failureThreshold) {
	        		//writer.println(Experiments.modus + "," + Experiments.numFireFighters + "," + Experiments.numFires
	        		//		+ "," + Experiments.seed + "," + Experiments.losSight + "," + Experiments.losComm + "," + "unsuccessful");
	        		writer.print(",NA");
	        		sim.stop();
	        	}
	        	
	        	
	        	Set<FireStatus> cells = roadModel.getObjectsOfType(FireStatus.class);
	        	for (FireStatus f : cells)
	        		f.tick(time);
	        	
	        }

	        @Override
	        public void afterTick(TimeLapse time) {
	        	
	        }
	      });
	    
	    if(gui){
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
		    	           .addColorAssociation(PheromoneNode.class, new RGB(200, 200, 200))
		    	     )
		    	     .with(new LOSRenderer())
		    	     //.with(new PheromoneRenderer())
		    	     ;
		    viewBuilder.show();
	    }

	    // in case a GUI is not desired, the simulation can simply be run by
	    // calling the start method of the simulator.
	    sim.start();
	}
	
	
	


	private static boolean isPointInBoundary(Point p) {
		return p.x > MIN_POINT.x+0.5 && p.x < MAX_POINT.x-0.5 && p.y > MIN_POINT.y+0.5 && p.y < MAX_POINT.y-0.5;
    }
}
