package mas;

import com.github.rinde.rinsim.core.TimeLapse;
import com.github.rinde.rinsim.core.model.comm.MessageContents;
import com.github.rinde.rinsim.geom.Point;

public class ContractTaskAnnouncement implements MessageContents {
	//public final Point elegibilityRange; // don't think this is necessary
	public final Point taskAbstraction; // the point where the fire is
	//public final Point bidSpecification; // as bid we expect a point, but i don't think it's necessary to express this here
	public final TimeLapse expirationTime; // deadline for sending bids
	
	public ContractTaskAnnouncement(/*Point er, */Point ta, /*Point bs, */TimeLapse et) {
		//elegibilityRange = er;
		taskAbstraction = ta;
		//bidSpecification = bs;
		expirationTime = et;
	}
}
