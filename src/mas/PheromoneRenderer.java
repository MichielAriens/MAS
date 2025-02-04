package mas;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import com.github.rinde.rinsim.core.model.ModelProvider;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.ModelRenderer;
import com.github.rinde.rinsim.ui.renderers.ViewPort;
import com.github.rinde.rinsim.ui.renderers.ViewRect;
import com.google.common.base.Optional;

public class PheromoneRenderer implements ModelRenderer {

	private static final float OFFSET = 0.2f; 

	Optional<RoadModel> rm;
	Optional<DefaultPDPModel> pm;

	PheromoneRenderer() {
		rm = Optional.absent();
		pm = Optional.absent();
	}

	@Override
	public void registerModelProvider(ModelProvider mp) {
		rm = Optional.fromNullable(mp.tryGetModel(RoadModel.class));
		//pm = Optional.fromNullable(mp.tryGetModel(DefaultPDPModel.class));
	}


	@Override
	public void renderStatic(GC gc, ViewPort vp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renderDynamic(GC gc, ViewPort vp, long time) {
		final Set<PheromoneNode> pheroms = rm.get().getObjectsOfType(PheromoneNode.class);
		synchronized (pheroms) {
			Set<PheromoneEdge> edges = new HashSet<>();
			for(PheromoneNode n : pheroms){
				edges.addAll(n.allEdges());
			}
			for(final PheromoneEdge e : edges){
				final Point p = e.getPointAllong(OFFSET);
				final int x = vp.toCoordX(p.x);// - 5;
				final int y = vp.toCoordY(p.y);// - 30;

				String text = e.getPheromone();
				System.out.println(text);
				
				final org.eclipse.swt.graphics.Point extent = gc.textExtent(text);
				//gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_BLUE));
				//gc.setBackground(new Color(gc.getDevice(), arg1, arg2, arg3));
				//gc.fillRoundRectangle(x - (extent.x / 2), y - (extent.y / 2), extent.x + 2, extent.y + 2, 5, 5);
				//gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));

				gc.drawText(text, x - (extent.x / 2) + 1, y - (extent.y / 2) + 1, true);
				
				//gc.drawPoint(x - (extent.x / 2) + 1, y - (extent.y / 2) + 1);
			}





		}
	}



@Override
public ViewRect getViewRect() {
	// TODO Auto-generated method stub
	return null;
}

}

