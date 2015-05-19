package fire;

import com.github.rinde.rinsim.geom.Point;

public class FireGrid {
	
	private FireStatus[][] grid;
	private final Point topLeft, bottomRight;
	private final int width, height;
	private final double cellSize;

	/**
	 * @param resolution The amount of cells horizontally (cells are square)
	 * @param topLeft
	 * @param bottomRight
	 */
	public FireGrid(double cellSize, Point topLeft, Point bottomRight){
		this.topLeft = topLeft; this.bottomRight = bottomRight;		
		this.cellSize = cellSize;
		this.width = (int) Math.ceil((bottomRight.x - topLeft.x)/cellSize);
		this.height = (int) Math.ceil((topLeft.y - bottomRight.y)/cellSize);
		grid = new FireStatus[height][width];
		for(int j = 0; j < width; j++){
			for(int i = 0; i < height; i++){
				grid[i][j] = new FireStatus.Dry();
			}
		}
	}
	
	private class Coord{
		public final int x,y;
				
		public Coord(int x, int y){
			this.x = x; this.y = y;
		}
	}
	
	/**
	 * Gets the cell containing the point provided. 
	 * @param p
	 * @return
	 */
	Coord convert(Point p){
		double x = (p.x - topLeft.x)/(bottomRight.x - topLeft.x);
		double y = (p.y - bottomRight.y)/(topLeft.y - bottomRight.y);
		return new Coord((int) (x * width), (int) (y * height));
	}
	
	Point convert(Coord c){
		return new Point(c.x*cellSize + topLeft.x, c.y*cellSize + bottomRight.y);
	}
	
	/**
	 * Light a cell and cells in a radius around it. 
	 * @param center
	 * @param radius
	 */
	public void light(Point center, float radius){
		Coord c = convert(center);
		int n = (int) Math.ceil(radius/cellSize);
		for(int i = c.x - n; i <= c.x + n; i++){
			for(int j = c.y - n; j <= c.y + n; j++){
				try{
					grid[i][j] = new FireStatus.Fire();
				}catch(IndexOutOfBoundsException e){}//ignore edges.
			}
		}
	}
	
	/**
	 * Extinguish cell and cells in a radius around it. 
	 * @param center
	 * @param radius
	 */
	public void extinguish(Point center, float radius){
		Coord c = convert(center);
		int n = (int) Math.ceil(radius/cellSize);
		for(int i = c.x - n; i <= c.x + n; i++){
			for(int j = c.y - n; j <= c.y + n; j++){
				try{
					grid[i][j] = new FireStatus.Wet();
				}catch(IndexOutOfBoundsException e){}//ignore edges.
			}
		}
	}
	

}
