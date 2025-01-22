package crypto.dcop.mgm.vanilla;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import common.framework.nodes.MessageHandler;
import crypto.dcop.DcopAgent;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;


public class Agent extends DcopAgent {

	public Agent(int agnetId, int domainPower, long seed) {
		super(agnetId);
		this.domainPower = domainPower;
		this.seed = seed;
		this.constraints = new HashMap<Integer, int[][]>();
		this.neighbors = new Vector<Agent>();
		registerMsgHandlers();
	}

	// Tools
	private Random random;
	private boolean debug = true	;
	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, "ID:" + ID + " " + logStr);
	}
	
	// COP info
	private Map<Integer, int[][]> constraints;
	private Vector<Agent> neighbors;
		
	
	
	// MGM
	boolean trigger;
	boolean running;
	private long seed;
	private int xIndex;
	private int yIndex;
	private int domainPower;
	private int round;
	private int bigC;
	private int bigCCounter;
	private Map<Integer, Integer> otherValues;
	private Map<Integer, Integer> otherSmallGi;
	
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


	@Override
	public void triggerAlgo() {
		trigger = true;
		round = 0;
	}

	public void Trigger(int round, double d) {
		//acknowledgeRound = d;
		
		trigger = true;
		round = 0;
	}
	
	public boolean Running() {
		return running;
	}
	
	
	private void sendMsgToNeighbors(Message msg) {
		for (Node agnet : neighbors) {
			sendMsgToNode(msg, agnet);
		}			
	}
			
	private void sendMsgToNeighborsPlus(Message msg) {
		// NeighborsPlus is nothing but sending the message to all neighbors and myself
		sendMsgToNeighbors(msg);
		handleMsg(this, msg);
	}
	
	private void BroadcastMsg(Message msg) {
		for (Agent a: neighbors) {
			send(msg, a);
		}
		// Also need to take part of the compare so sending the message to myself
		handleMsg(this, msg);
	}

	
	public void registerMsgHandlers() {
		// TODO P0 register all messages
		
		registerMsgHandlers(SelectedIndexMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				handleSelectedIndexMsg(sender, (SelectedIndexMsg) msg);
			}
		});

		registerMsgHandlers(SmallGiMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				handleSmallGiMsg(sender, (SmallGiMsg) msg);
			}
		});

		registerMsgHandlers(SmallCiMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				handleSmallCiMsg(sender, (SmallCiMsg) msg);
			}
		});

		
	}

	
	// STATE: INIT
	private void kickStartMGM() {
		debug(debug, "Kick start MGM");
		// Set running flags
		trigger = false;
		running = true;
		// Set i/s 
		if (seed == 0) {
			random = new Random();
		} else {
			random = new Random(seed+1000+ID);	
		}
		
		
		otherValues = new HashMap<Integer, Integer>();
		otherSmallGi = new HashMap<Integer, Integer>();
		// Set random index -->#2
		xIndex = random.nextInt(domainPower);
		bigC = 1;
		debug(debug, ">> index: " + xIndex);
		
		mgmStartNewRound();		
	}
	
	
	private void mgmStartNewRound() {
		debug(debug, String.format("Starting new round %d. C = %d",round, bigC));
		round++;
		if (bigC <= 0) {
			endOfRun();
			return;
		}
		sendIndexToNeighbors(xIndex);
	}
	
	private void sendIndexToNeighbors(int index) {
		debug(debug, String.format("Sending index [%d] to all neighbors", xIndex));
		SelectedIndexMsg msg = new SelectedIndexMsg(index);
		sendMsgToNeighbors(msg);
	}
			
	private void handleSelectedIndexMsg(Node sender, SelectedIndexMsg msg) {
		debug(debug, String.format("Got index msg from %d. index [%d] other size [%d]", sender.ID, msg.index, otherValues.size()));
		otherValues.put(sender.ID, msg.index);
		// Wait until all values from all neighbors
		if (otherValues.size() != neighbors.size()) {
			return;			
		}
		calcSmallGi();
	}
	
	private void calcSmallGi() {
		debug(debug, String.format("calcing small gi"));
		int smallGi = Integer.MIN_VALUE;
	
		// iterate over all the domain
		for (int yi = 0; yi < domainPower; yi++) {
			int gi = 0;
			debug(debug, String.format("calcing small gi[%d] using yi[%d]", gi, yi));
			// calc gi = current cost - the cost if using index i
			for (Node other: neighbors) {
				gi = gi + constraints.get(other.ID)[xIndex][otherValues.get(other.ID)];
				gi = gi - constraints.get(other.ID)[yi][otherValues.get(other.ID)];
				debug(debug, String.format("calcing small gi[%d] other [%d]", gi, other.ID));
			}
			if (gi > smallGi) {
				smallGi = gi;
				yIndex = yi;
			}			
		}

		debug(debug, String.format("small gi[%d] yIndex [%d]", smallGi, yIndex));
		SmallGiMsg msg = new SmallGiMsg(smallGi);
		sendMsgToNeighborsPlus(msg);
		
		otherValues.clear();
	}
	
	private void handleSmallGiMsg(Node sender, SmallGiMsg msg) {
		debug(debug, String.format("Got small gi msg from %d. index [%d] other size [%d]", sender.ID, msg.smallGi, otherSmallGi.size()));
		otherSmallGi.put(sender.ID, msg.smallGi);
		// Wait until all values from all neighbors
		if (otherSmallGi.size() != neighbors.size() + 1) {
			return;			
		}
	
		int ci = 0;
		
		// Look for the max small-g
		// init with my smallGi
		int bigGi = otherSmallGi.get(ID);
		int k = ID;
		
		// loop on other smallGi
		for (Node other: neighbors) {
			debug(debug, String.format("calcing bigGi [%d] k [%d]", bigGi, k));
			debug(debug, String.format("calcing otherID [%d] gi [%d]", other.ID, bigGi));
			int gi = otherSmallGi.get(other.ID);
			if ((gi > bigGi) && (other.ID < k)) {
				bigGi = gi;
				k = other.ID;
			}
		}
						
		if (bigGi > 0) {
			if ((otherSmallGi.get(ID) == bigGi) && (ID == k)) {
				xIndex = yIndex;
				ci = 1;
			}
		}
		debug(debug, String.format("calcing bigGi [%d] k [%d] ci [%d]", bigGi, k, ci));
		otherSmallGi.clear();
		bigC = 0;
		bigCCounter = 0;
		SmallCiMsg ciMsg = new SmallCiMsg(ci);
		BroadcastMsg(ciMsg);	
	}
	
	private void handleSmallCiMsg(Node sender, SmallCiMsg msg) {
		bigC = bigC + msg.smallCi;
		bigCCounter++;
		debug(debug, String.format("Got small ci msg from %d. index [%d] counter [%d] bigC %d", sender.ID, msg.smallCi, bigCCounter, bigC));
		if (bigCCounter != neighbors.size() + 1) {
			return;			
		}
		mgmStartNewRound();
	}
	
	private void endOfRun() {
		debug(debug, String.format("done xIndex [%d]", xIndex));
		running = false;
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

	@Override
	public void logState() {
		DebugCOPInfo(debug);
	}
	
	@Override
	public void init() {
	}

	@Override
	public boolean doneStatus() {
		return !running;
	}

	@Override
	public int assignment() {
		return xIndex;
	}

	@Override
	public void postStep() {
		if (trigger) {
			kickStartMGM();
		}
	}

}
