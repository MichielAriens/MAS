package mas;

import java.util.LinkedList;
import java.util.List;

/**
 * Controls the ant on an abstract level.
 * @author Michiel
 *
 */
public class AntAI {
	
	private enum State{
		Forward,Backward; 
	}
	
	private PheromoneNode node;
	private List<PheromoneEdge> history = new LinkedList<PheromoneEdge>();
	private double pathLength = 0;
	private State state = State.Forward;
	
	public AntAI(PheromoneNode home){
		this.node = home;
	}
	
	public PheromoneNode getNode(){
		return this.node;
	}
	
	public String getState(){
		return "" + state;
	}
	
	public void switchMode(){
		if(state == State.Forward){
			setBackward();
		}else{
			setForward();
		}
	}
	
	private void setBackward() {
		state = State.Backward;
		pathLength = 0;
		for(PheromoneEdge e : history){
			pathLength += e.getDistance();
		}
	}

	private void setForward() {
		state = State.Forward;
		history = new LinkedList<PheromoneEdge>();
	}
	
	

	public PheromoneNode nextNode(AntFireFighter fireFighter){
		if(state == State.Forward){
			return nextForwardNode(fireFighter);
		}else{
			return nextBackNode(fireFighter);
		}
	}
	
	private PheromoneNode nextForwardNode(AntFireFighter fireFighter){
		PheromoneEdge nextEdge = node.chooseEdge(fireFighter);
		node = nextEdge.getOtherNode(node);
		history.add(nextEdge);
		return node;
	}
	
	private PheromoneNode nextBackNode(AntFireFighter fireFighter){
		PheromoneEdge nextEdge = history.remove(history.size() - 1);
		PheromoneNode other = nextEdge.getOtherNode(node);
		other.promote(nextEdge, 1/pathLength);
		node = other;
		return node;
	}

}
