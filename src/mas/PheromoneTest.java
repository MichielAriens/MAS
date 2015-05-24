package mas;

import static org.junit.Assert.*;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import com.github.rinde.rinsim.geom.Point;

public class PheromoneTest {

	@Test
	public void test() {
		RandomGenerator rnd = new MersenneTwister(2);
		AntPheromone a = new AntPheromone(new Point(0, 0), rnd);
		AntPheromone b = new AntPheromone(new Point(1, 0), rnd);
		AntPheromone c = new AntPheromone(new Point(2, 0), rnd);
		a.addChild(b);
		a.addChild(c);
		
		c.setWeight(2);
		
		for(int i = 0; i < 10; i++){
			System.out.println(a.getAChild().getWeight());
		}
	}

}
