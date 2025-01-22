package projects.dcopproj;

import projects.dcopproj.nodes.edges.WEdge;

public class Mwoe {

	private int _startId;
	private int _endId;
	private long _edgeId;
	private int _weight;
	
	
	public Mwoe(int startId,
			     int endId,
			     long edgeId,
			     int weight) {
		_startId = startId;
		_endId = endId;
		_edgeId = edgeId;
		_weight = weight;
	}
	
	public Mwoe(WEdge edge) {
		_startId = edge.startNode.ID;
		_endId = edge.endNode.ID;
		_edgeId = edge.getID();
		_weight = edge.Weight();		
	}
	
	public void Reinit(WEdge edge) {
		_startId = edge.startNode.ID;
		_endId = edge.endNode.ID;
		_edgeId = edge.getID();
		_weight = edge.Weight();		
	}
	
	public boolean isLighter(WEdge edge) {
		if (_weight < edge.Weight()) {
			return true;
		}
		// Break even using edge ID
		if ((_weight == edge.Weight()) && (_edgeId < edge.getID())) {
			return true;			
		}		
		return false;
	}

	public boolean isLighter(Mwoe other) {
		if (other == null) {
			return true;
		}
		if (_weight < other._weight) {
			return true;
		}
		// Break even using edge ID
		if ((_weight == other._weight) && (_edgeId < other._edgeId)) {
			return true;			
		}		
		return false;
	}
	
	public int StartId( ) {
		return _startId;
	} 

	public int EndId( ) {
		return _endId;
	} 
	
	public boolean Match(Mwoe other) {
		return ((_startId == other._endId) && (_endId == other._startId)); 
	}
	
	public String toString() {
		return "u[" + _startId + "] v[" + _endId +  "]  w[" + _weight + "] e[" + _edgeId + "]";
	}
}
