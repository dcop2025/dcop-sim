package crypto.dcop.mgm.secure;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import crypto.dcop.DcopAgent;
import crypto.utils.shamir.Shared;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class Agent extends DcopAgent{
	// Tools
	private Random random;
	private boolean debug = true	;
	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, "ID:" + ID + " " + logStr);
	}
	
	// COP info
	private Map<Integer, int[][]> constraints;
	private Vector<Agent> neighbors;

	private void DebugCOPInfo(boolean xdebug) {
		debug(xdebug, "COP Info");
		for (int key : constraints.keySet()) {
			Global.log.logln(xdebug, "Other ID:" + key);
			for (int i = 0; i < constraints.get(key).length; i++) {
				Global.log.log(xdebug, "\t");
				for (int j = 0; j < constraints.get(key)[i].length; j++) {
					Global.log.log(xdebug, " "+ constraints.get(key)[i][j]);
				}
				Global.log.log(xdebug, "\n");
			}
		}
	}
	
	
	private class NeighborsKey {
		public int iID;
		public int jID;
		
		public NeighborsKey fix() {
			if (iID <= jID) {
				return this;
			}
			int tmp = iID;
			iID = jID;
			jID = tmp;
			return this;
		}
	}

	private class Neighborhood {
		private Map<NeighborsKey, Shared> neighborhoodMap;
		public Neighborhood() {
			neighborhoodMap = new HashMap<Agent.NeighborsKey, Shared>();			
		}
		
		public void put(Agent.NeighborsKey key, Shared shared) {
			neighborhoodMap.put(key.fix(), shared);
		}
		
		public Shared get(Agent.NeighborsKey key) {
			return neighborhoodMap.get(key.fix());
		}
	}
	
	private BigInteger prime;
	private long seed;
	
	public Map<String, Integer> counters;
	private Map<String, Shared> sharedStorage;	
	private Map<String, Integer> waiters;
	 
	private boolean trigger;
	private boolean running;
	private int shamirThreshold;

	
	private int round;
	private int domainPower;
	private int xIndex; 
	
	private boolean TickWaiters(String key, int waitTil) {
		// Also ack back that the compare result is ready
		Integer wait = waiters.get(key);
		if (wait == null) {
			wait = 0;
		} 
		debug(debug, "Tick " + key + "  waiter: " + wait);
		wait++;
		waiters.put(key, wait);
		if (wait != waitTil) {
			return false;			
		}
		waiters.remove(key);
		return true;
	}

	private void TickCounter(String key, int added) {
		Integer count = counters.get(key);
		if (count == null) {
			count = 0;
		} 
		count = count + added;
		counters.put(key, count);
	}
			
	
	
	public void registerMsgHandlers() {
		// TODO P0 register all messages
	}
	
	
	private void SendMsg(int agnetID, Message msg) {
		if (agnetID == ID) {
			// If the message is for me, no need to send it
			handleMsg(this, msg);
			return;
		}
		Agent a = getAgentByID(agnetID);		
		if (a == null) {
			// Log an error before panic
			debug(true, "ERROR: Agent " + agnetID + " wasn't found in agent " + ID);
		}
		sendMsgToNode(msg, a);
	}

		
	private void BroadcastMsg(Message msg) {
		for (Agent a: neighbors) {
			send(msg, a);
		}
		// Also need to take part of the compare so sending the message to myself
		handleMsg(this, msg);
	}

		
	public void Trigger() {		
		trigger = true;
		round = 0;
	}
	
	public boolean Running() {
		return running;
	}
	public Agent(int agentID, int domainPower, BigInteger prime, long seed) {
		super(agentID);
		this.domainPower = domainPower;
		this.constraints = new HashMap<Integer, int[][]>();
		this.neighbors = new Vector<Agent>();
		this.prime = prime;
		this.running = false;
		this.trigger = false;
		this.seed = seed;
		registerMsgHandlers();
	}
		
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}
		
	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
		// we could remove routing-table entries that use this neighbor
	}

	@Override
	public void preStep() {
	}

	/*
	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message m = inbox.next();
			debug(false, "got " + m.getClass() + " from: " + inbox.getSender().ID);
			if (m instanceof OMessage) {
				OMessage msg = (OMessage)m;
				msg.action(this,  inbox.getSender());
			}
		}
	}*/

	@Override
	public void postStep() {
		if (trigger) {
			kickStart();
		}
	}
	
	public int domainPower() {
		return domainPower;
	}

	// Add a constraints matrix with another agetn
	public void addConnectionConstraints(Agent other, int[][] constraints) {
		this.constraints.put(other.ID, constraints);
		neighbors.add(other);
		// only one agent needs to add a bi connection, let it be the agent with higher id  
		if (ID > other.ID) {
			addBidirectionalConnectionTo(other);
		}
	}
		
	public void addSupportConnection(Agent other) {
		if (this.constraints.containsKey(other.ID)) {
			return;
		}
		
		int[][] zeros = new int[domainPower][other.domainPower];
		this.constraints.put(other.ID, zeros);
		neighbors.add(other);
		// only one agent needs to add a bi connection, let it be the agent with higher id  
		if (ID > other.ID) {
			addBidirectionalConnectionTo(other);
		}
	}
	
	//
	public int getConstraint(int otherID, int index, int otherIndex) {
		if (!constraints.containsKey(otherID)) {
			return 0;
		}
		
		return constraints.get(otherID)[index][otherIndex];		
	}

	private Agent getAgentByID(int otherID) {
		Agent other = null;		
		for (Agent a: neighbors) {
			if (a.ID == otherID) {
				other = a;
			}
		}
		return other;
	}

	@Override
	public void triggerAlgo() {
		trigger = true;
	}
	
	@Override
	public boolean doneStatus() {
		return !running;
	}

	@Override
	public void logState() {
		DebugCOPInfo(debug);
	}

	@Override
	public int assignment() {
		return xIndex;
	}

	@Override
	public int getInternal(String key) {
		Integer count = counters.get(key);
		if (count == null) {
			return 0;
		}
		return count;
	}


	private void kickStart() {
		debug(debug, "Kick start PMGM");
		// Set running flags
		trigger = false;
		running = true;
		// Set i/s 
		if (seed == 0) {
			random = new Random();
		} else {
			random = new Random(seed+1000+ID);	
		}
		
		sharedStorage = new HashMap<String, Shared>();
		waiters = new HashMap<String, Integer>();
		counters = new HashMap<String, Integer>();
		shamirThreshold = (int) Math.floor((double)(neighbors.size()+1)/2);
		

		// Set random index -->#2
		xIndex = random.nextInt(domainPower);
		debug(debug, ">> index: " + xIndex);
		
		//pdsaStartNewRound();		

	}
	
}
