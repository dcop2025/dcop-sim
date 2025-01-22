package projects.dcopproj.nodes.edges;

import java.awt.Color;
import java.awt.Graphics;

import projects.dcopproj.Mwoe;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.gui.GraphPanel;
import sinalgo.gui.helper.Arrow;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.io.eps.EPSOutputPrintStream;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.BidirectionalEdge;
import sinalgo.tools.Tools;
import sinalgo.tools.statistics.Distribution;

public class WEdge extends BidirectionalEdge {
	
	public int _weight;
	
	public WEdge() {
		try {
			 strokeWidth= Configuration.getIntegerParameter("BigEdge/strokeWidth");
		} catch(CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}

		java.util.Random rand = Distribution.getRandom();
		_weight = rand.nextInt(1000000000);
		//_weight = rand.nextInt(100);
	}
	
	public void SetWeight(int weight) {
		this._weight = weight;
	}
	
	public int Weight() {
		return _weight;
	}
	
	public Node otherNode(Node me) {		
		return (startNode == me) ? endNode : startNode;
	}

	public int lowNodeID() {
		return (this.startNode.ID < this.endNode.ID) ? this.startNode.ID : this.endNode.ID;
	}

	public int highNodeID() {
		return (this.startNode.ID < this.endNode.ID) ? this.endNode.ID : this.startNode.ID;
	}

	
	public boolean isLighter(WEdge other) {
		if (_weight < other._weight) {
			return true;
		}
		// Break even by edge ID
		if ((_weight == other._weight)  && (getID() < other.getID())) {
			return true;
		}
		return false;
	}
	
	public boolean isLighter(Mwoe other) {
		if (other == null) {
			return true;
		} 
		// return the ops of this 
		return !other.isLighter(this);
	}
	
	public String toString( ) {
		return super.toString() + " Weight " + _weight;
	}
////////////////////////////
	
	int strokeWidth;


	public void draw(Graphics g, PositionTransformation pt) {
		Position p1 = startNode.getPosition();
		pt.translateToGUIPosition(p1);
		int fromX = pt.guiX, fromY = pt.guiY; // temporarily store
		Position p2 = endNode.getPosition();
		pt.translateToGUIPosition(p2);
		
		if((this.numberOfMessagesOnThisEdge == 0)&&
				(this.oppositeEdge != null)&&
				(this.oppositeEdge.numberOfMessagesOnThisEdge > 0)){
			// only draws the arrowHead (if drawArrows is true) - the line is drawn by the 'opposite' edge
			Arrow.drawArrowHead(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
		} else {
			if(numberOfMessagesOnThisEdge > 0) {
				Arrow.drawArrow(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
				g.setColor(getColor());
				GraphPanel.drawBoldLine(g, fromX, fromY, pt.guiX, pt.guiY, strokeWidth);
			} else {
				Arrow.drawArrow(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
			}
		}
	}
	
	public void drawToPostScript(EPSOutputPrintStream pw, PositionTransformation pt) {
		pt.translateToGUIPosition(startNode.getPosition());
		double eSX = pt.guiXDouble;
		double eSY = pt.guiYDouble;
		pt.translateToGUIPosition(endNode.getPosition());
		Color c = getColor();
		pw.setColor(c.getRed(), c.getGreen(), c.getBlue());
		if(numberOfMessagesOnThisEdge > 0) {
			pw.setLineWidth(0.5 * strokeWidth); // bold line
		} else {
			pw.setLineWidth(0.5);
		}
		
		if(Configuration.drawArrows){
			pw.drawArrow(eSX, eSY, pt.guiXDouble, pt.guiYDouble);
		}
		else{
			pw.drawLine(eSX, eSY, pt.guiXDouble, pt.guiYDouble);
		}
	}

}
