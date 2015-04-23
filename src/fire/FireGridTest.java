package fire;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.rinde.rinsim.geom.Point;

public class FireGridTest {

	@Test
	public void test() {
		FireGrid fg = new FireGrid(0.5, new Point(10,20), new Point(30, 10));
		Point p = new Point(20, 15);
		assertEquals(p, fg.convert(fg.convert(p)));
		p = new Point(15, 19);
		assertEquals(p, fg.convert(fg.convert(p)));
	}

}
