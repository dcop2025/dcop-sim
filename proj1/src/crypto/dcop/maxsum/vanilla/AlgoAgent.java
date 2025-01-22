package crypto.dcop.maxsum.vanilla;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import common.framework.nodes.MessageHandler;
import common.framework.nodes.NodeService;
import common.framework.nodes.ONodeAlgo;
import common.framework.nodes.SyncPoint;
import common.framework.nodes.Variabler;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class AlgoAgent implements ONodeAlgo {

	private boolean debug;
	
	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, "ID:" + ID + " " + logStr);
	}

	
	static final String r_key = "R";
	static final String r_sync = "sync-R";
	
	private Variabler variabler;
	private NodeService nodeService;
	private SyncPoint syncPoint;
	private int ID;
	private int domainPower;

	private Map<Integer, int[][]> constraints;
	private Map<Integer, Node> functionNeighborhood;
	
	private int round;
	private int lastRound;
	
	private int xIndex;
	
	private boolean runningStatus;
		
	public AlgoAgent(NodeService nodeService, Variabler variabler, SyncPoint syncPoint, int domainPower) {
		this.variabler = variabler;
		this.nodeService = nodeService;
		this.syncPoint = syncPoint;
		this.ID = this.nodeService.ID();
		
		this.domainPower = domainPower;
		// TODO remove this
		constraints = new HashMap<Integer, int[][]>();
		functionNeighborhood = new HashMap<Integer, Node>();
		
		runningStatus = false;
		lastRound = 3;
	}
	
	public void addConstraints(int otherID, int[][] matrix) {
		constraints.put(otherID, matrix);
	}
	
	public void addFunctionNode(int otherId, Node node) {
		functionNeighborhood.put(otherId, node);
	}
	
	public void start() {
		runningStatus = true;
		variabler.clear();
		// Set zero round value
		round = 0;
		xIndex = -1;

		// Zero out Rs
		for (Integer fID : this.functionNeighborhood.keySet()) {
			for (int i = 0; i < domainPower; i++) {
				// Inject R^0_i
				variabler.Set(rKey(round, fID, i), BigInteger.ZERO);				
			}
		}
		
		kickStartRound();
	}
	
	public int xIndex() {
		return xIndex;
	}
	
	private void kickStartRound() {
		round++;
		debug(debug, "kick start round: " + round);
		if (round == lastRound) {
			finishRun();
			return;
		}
		
		// Send a Q message for each neighbor function node
		for (Integer key : this.functionNeighborhood.keySet()) {
			sendQsToFunction(key);
		}		
	}
	
	private void finishRun() {
		debug(debug, "finish running");
		BigInteger[] Ms = new BigInteger[domainPower];
		for (int x = 0; x < domainPower; x++) {
			Ms[x] = BigInteger.ZERO;
		}
		
		// iterate over all function neighborhood
		for (Integer fID : this.functionNeighborhood.keySet()) {
			for (int x = 0; x < domainPower; x++) {
				// use the prev round Rs
				String key = rKey(round - 1,fID, x);
				BigInteger Rf = this.variabler.Get(key);
				if (Rf == null) {
					debug(true, "ERROR: Unable to find key:" + key);
				}
				Ms[x] = Ms[x].add(Rf);				
			}
		}

		BigInteger min = Ms[0];
		xIndex = 0;
		for (int x = 0; x < domainPower; x++) {
			if (Ms[x].compareTo(min) == -1) {
				min = Ms[x];
				xIndex = x;				
			}
		}
		debug(debug, "selected index: " + xIndex);
		runningStatus = false;
	}
	
	private void sendQsToFunction(int targetID) {
		BigInteger[] Qs = new BigInteger[domainPower];
		for (int i = 0; i < domainPower; i++) {
			Qs[i] = BigInteger.ZERO;
		}
		
		// iterate over all function neighborhood
		for (Integer fID : this.functionNeighborhood.keySet()) {
			// Skip the the otherID
			if (fID == targetID) {
				continue;
			}
			
			for (int x = 0; x < domainPower; x++) {
				// use the prev round Rs
				String key = rKey(round - 1,fID, x);
				BigInteger Rf = this.variabler.Get(key);
				if (Rf == null) {
					debug(true, "ERROR: Unable to find key:" + key);					
				}
				Qs[x] = Qs[x].add(Rf);
			}
		}
		// TODO log Q
		UpdateQsMsg msg = new UpdateQsMsg(targetID, round, Qs);
		Node functionNode = functionNeighborhood.get(targetID);
		if (functionNode == null) {
			// TODO handle error
		}
		debug(debug, "Sending Qs( " + msg.qString() + ") to " + functionNode.ID + " on node: " + targetID); 
		nodeService.sendMsgToNode(msg, functionNode);		
	}
	
	public void UpdateRsMsgHandler(Node sender, UpdateRsMsg rMsg) {
		debug(debug, "Handleing a Rs update message from " + sender.ID + " Rs:" + rMsg.rString() + " round " + round);
		
		// Store R in Q
		for (int x = 0; x < rMsg.r().length; x++) {
			this.variabler.Set(rKey(round, rMsg.otherID(), x), rMsg.r()[x]);
		}
		// Wait for all Rs message to received 
		boolean done = syncPoint.tickSync(rSyncKey(round), functionNeighborhood.size());
		if (!done) {
			debug(debug, "still waiting for r msg (need " + functionNeighborhood.size() + ")");
			return;
		}
		// The agent has all the Rs from all function neighbors, let's go to the next round
		debug(debug, "going to next round");
		kickStartRound();
	}
	
	private String rKey(int round, int otherID, int index ) {
		return r_key + "^" + round + "_" + otherID + "-->" + ID  + "(" + index + ")"; 
	}

	private String rSyncKey(int round) {
		return r_sync + "-" + round;
	}

	public void init() {
		// TODO Auto-generated method stub
		
	}

	public boolean doneStatus() {
		// TODO Auto-generated method stub
		return !runningStatus;
	}
	
	public void registerMsgHandlers() {
		nodeService.registerMsgHandlers(UpdateRsMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				UpdateRsMsgHandler(sender, (UpdateRsMsg) msg);
			}
		});
	}
	
	public void logState() {
		boolean debug = false;
		debug(debug, "State of Agnet "+ ID);
		debug(debug, "\tDomain power "+ domainPower);
		debug(debug, "\tRound ("+ round + "/" + lastRound + ")");
		debug(debug, "\tFunction Neighborhood:");
		// Send a Q message for each neighbor function node
		for (Integer otherId : this.functionNeighborhood.keySet()) {
			debug(debug, "\t\tWith " + otherId + " by " + functionNeighborhood.get(otherId).ID);
		}
	}

	public int assignment() {
		return xIndex;
	}
	
	public int getInternal(String key) {
		return round;
	}

}
