package crypto.dcop.dsa.vanilla.barins;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import common.framework.nodes.MessageHandler;
import common.framework.nodes.NodeService;
import common.framework.nodes.ONodeAlgo;
import crypto.dcop.IDcopBrain;
import crypto.dcop.Problem.ConstraintsMatrix;
import crypto.dcop.dsa.vanilla.messages.NotifySelectedValueMsg;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class AgentBarin implements ONodeAlgo, IDcopBrain {

	private int domainPower;
	private Map<Integer, int[][]> constraints;
	private Vector<Node> neighbors;
	private int xIndex;
	
	private int round;
	private double p = 0.7;
	
	private Map<Integer, Integer> otherValues;

	private Random random;
		
	private boolean runningDSA;
	
	private long seed;
	
	private boolean debug;
	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, "ID:" + ID + " " + logStr);
	}

	private int ID;
	private NodeService nodeService;

	public AgentBarin(NodeService nodeService, int domainPower, long seed) {
		this.nodeService = nodeService;
		this.ID = nodeService.ID();
		this.constraints = new HashMap<Integer, int[][]>();
		this.domainPower = domainPower;
		this.neighbors = new Vector<Node>();
		this.seed = seed;
	}
	
	public void registerMsgHandlers() {
		nodeService.registerMsgHandlers(NotifySelectedValueMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				notifySelectedValueMsgHandler(sender, (NotifySelectedValueMsg) msg);
			}
		});				
	}
	
	
	public void addNeighbor(Node neighbor) {
		this.neighbors.add(neighbor);
	}
	
	private void kickStartDSA() {
		debug(debug, "Kick start DSA");
		// TODO P0 DebugCOPInfo(true);
				
		// The first thing that needs to be done is to select a random value, and share it with others		
		xIndex = random.nextInt(domainPower);
		debug(debug, ">> index: " + xIndex);
		sendSelectValueToNeighbors(xIndex);
	}

	
	private void sendSelectValueToNeighbors(int index) {
		// Create a message
		NotifySelectedValueMsg msg = new NotifySelectedValueMsg(index);
		// TX that message
		for (Node agnet : neighbors) {
			nodeService.sendMsgToNode(msg, agnet);
		}		
	}
	
	private void notifySelectedValueMsgHandler(Node sender, NotifySelectedValueMsg msg) {
		otherValues.put(sender.ID, msg.index);
		// Wait until all values from all neighbors
		if (otherValues.size() != neighbors.size()) {
			return;			
		}
		handleEndDSARound();	
	}
	
	private void handleEndDSARound() {
		// TODO handle error that index is -1
		this.xIndex = selectMinIndex();
		// once we done going over other value clear storage
		otherValues.clear();
				
		sendSelectValueToNeighbors(this.xIndex);
		round--;
		if (round == 0) {
			debug(debug, "End of DSA: Index: " + xIndex);
			runningDSA = false;
			return;
		}
		
		
	}
	
	private int selectMinIndex() {
		boolean skipRound = (p < random.nextFloat());
		if (skipRound) {
			return xIndex;
		}

		int cost = Integer.MAX_VALUE;
		int newIndex = -1;
		int tmpCost;
		for (int i = 0; i < domainPower; i++) {
			tmpCost = 0;
			for (int key : otherValues.keySet()) {
				int keyCost = constraints.get(key)[i][otherValues.get(key)];
				tmpCost += keyCost;	
				debug(debug, "key: " + key + " key-cost: " + key + " tmp cost: " + tmpCost);
			}
			if (tmpCost < cost) {
				cost = tmpCost;
				newIndex = i;
			}
		}
		return newIndex;
	}

	public void init() {
		// TODO Auto-generated method stub
		if (seed == 0) {
			random = new Random();
		} else {
			random = new Random(seed);
		}
		runningDSA = true;
		round = 7;
		
		otherValues = new HashMap<Integer, Integer>();
	}

	public void start() {
		// TODO Auto-generated method stub
		kickStartDSA();		
	}

	public boolean doneStatus() {
		// TODO Auto-generated method stub
		return !runningDSA;
	}

	public void logState() {
		 
		debug(debug, "COP Info");
		for (int key : constraints.keySet()) {
			Global.log.logln(debug, "Other ID:" + key);
			for (int i = 0; i < constraints.get(key).length; i++) {
				Global.log.log(debug, "\t");
				for (int j = 0; j < constraints.get(key)[i].length; j++) {
					Global.log.log(debug, " "+ constraints.get(key)[i][j]);
				}
				Global.log.log(debug, "\n");
			}
		}		
	}

	public void installConstraintsMatrix(ConstraintsMatrix constraints) {		
		this.constraints.put(constraints.getOtherId(ID), constraints.getMatrix(ID));
	}

	public int assignment() {
		return xIndex;
	}


	public int getInternal(String key) {
		return round;
	}

}
