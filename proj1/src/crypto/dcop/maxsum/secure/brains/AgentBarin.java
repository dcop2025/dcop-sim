package crypto.dcop.maxsum.secure.brains;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import common.framework.nodes.MessageHandler;
import common.framework.nodes.NodeService;
import common.framework.nodes.ONodeAlgo;
import common.framework.nodes.SyncPoint;
import common.framework.nodes.Variabler;
import crypto.dcop.maxsum.secure.messages.InjectQsMsg;
import crypto.dcop.maxsum.secure.messages.InjectRsMsg;
import crypto.dcop.maxsum.secure.messages.MinIndexReqMsg;
import crypto.dcop.maxsum.secure.messages.MinIndexRespondMsg;
import crypto.dcop.maxsum.secure.messages.ProcessWsRequestMsg;
import crypto.dcop.maxsum.secure.messages.ProcessWsRespondMsg;
import crypto.utils.Paillier;
import crypto.utils.PaillierMgr;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class AgentBarin implements ONodeAlgo {

	// debug 
	private boolean debug;
	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, "ID:" + agentIndex + " " + logStr);
	}
		
	// E Paillier
	static final String e_paillier_key = "E";
	private String ePaillierKey(int index) {
		return e_paillier_key + "-" + index;
	}

	// F Paillier
	static final String f_paillier_key = "F";
	private String fPaillierKey(int index) {
		return f_paillier_key + "-" + index;
	}

	
	// R variable
	static final String r_key = "R";
	private String rKey(int round, int affinity, int source, int target, int index) {
		return String.format("R^%d,%d_%d-->%d(%d)", round, affinity, source, target, index);
	}


	// Q variable
	static final String q_key = "Q";
	private String qKey(int round, int affinity, int source, int target, int index) {
		return String.format("Q^%d,%d_%d-->%d(%d)", round, affinity, source, target, index);
	}

	// init r waiter key
	static final String init_r = "Init-R";
	private String initRsWaiter(int round) {
		return init_r + "-" + round;
	}
	
	private Variabler variabler;
	private NodeService nodeService;
	private SyncPoint syncPoint;
	private int agentIndex;
	//private int ID;
	private int domainPower;
	
	private Map<Integer, Node> functionNeighborhood;
	
	private int round;
	private int lastRound;
	
	private int xIndex; // this is the index selected by the agent
	
	private boolean runningStatus;
	
	private Random random;
	
	private BigInteger p;
	
	PaillierMgr paillierMgr;
		
	public AgentBarin(int index, 
			NodeService nodeService, 
			Variabler variabler, 
			SyncPoint syncPoint, 
			int domainPower, 
			PaillierMgr paillierMgr,
			BigInteger p) {
		this.agentIndex = index;
		this.variabler = variabler;
		this.nodeService = nodeService;
		this.syncPoint = syncPoint;
		this.p = p;
		//this.ID = this.nodeService.ID();
		
		this.domainPower = domainPower;
		functionNeighborhood = new HashMap<Integer, Node>();
		
		runningStatus = false;
		lastRound = 100;
		// Setting up paillier keys
		paillierMgr.put(ePaillierKey(this.agentIndex), new Paillier());
		paillierMgr.put(fPaillierKey(this.agentIndex), new Paillier());
		this.paillierMgr = paillierMgr;
	}
		
	public void addFunctionNode(int agentId, Node node) {
		functionNeighborhood.put(agentId, node);
	}
	
	
	public void registerMsgHandlers() {
		nodeService.registerMsgHandlers(InjectRsMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				injectRsMsgHandler(sender, (InjectRsMsg) msg);
			}
		});

		nodeService.registerMsgHandlers(ProcessWsRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				processWsRequestMsgHandler(sender, (ProcessWsRequestMsg) msg);
			}
		});
		

		nodeService.registerMsgHandlers(MinIndexRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				minIndexRespondMsgHandler(sender, (MinIndexRespondMsg) msg);
			}
		});

		
	}
	
	public void start() {		
		runningStatus = true;
		variabler.clear();
		random = new Random();
		// Set zero round value
		round = 0;
		xIndex = random.nextInt(domainPower); // have a default value 
	}

	private void injectRsMsgHandler(Node sender, InjectRsMsg msg) {
		// ASSERT msg.round == this.round
		// ASSERT msg.source == sender.ID & sender.ID is in function
		// ASSERT msg.target == this.ID
		// ASSERT len(msg.r) == domain power 
				
		debug(debug, String.format("Handling Inject Rs Msg %s", msg.toString(true)));
		for (int x = 0; x < domainPower; x++) {
			// Store the local Rs (note that they are enc with Fi)
			variabler.Set(rKey(msg.round, msg.source, msg.source, msg.target, x), msg.local[x]);
			if (msg.withRemote == true) {
				// Store the remote Rs
				variabler.Set(rKey(msg.round, msg.target, msg.source, msg.target, x), msg.remote[x]);				
			}
		}
		// Wait until all Rs are ready
		boolean done = syncPoint.tickSync(initRsWaiter(msg.round), functionNeighborhood.size());
		if (!done) {
			return;
		}
		kickStartRound();
	}
	
	private void kickStartRound() {
		round++;
		debug(debug, "kick start round: " + round);
		if (round == lastRound) {
			kickStartLastRound();
			debug(true, "Last round: " + round);
			return;
		}
		
		// Start protocol one with all functions neighbors
		for (Integer otherId : this.functionNeighborhood.keySet()) {
			calcQs(otherId);
		}		
	}
	
	private void calcQs(int otherId) {
		debug(debug, String.format("Calculating Qs for: %d", otherId));
		Node functionNode = functionNeighborhood.get(otherId);
		if (functionNode == null) {
			debug(true, "ERROR: didn't found function node" + otherId);
		}

		// At this point agent got all the local and remote Rs from all function neighbors
		BigInteger[] localQs = new BigInteger[domainPower];
		BigInteger[] Ws = new BigInteger[domainPower];
		for (int x = 0; x < domainPower; x++) {
			localQs[x] = BigInteger.ZERO;
			Ws[x] = BigInteger.ONE;
		}

		
		// Iterate over all function neighborhood
		for (Integer fID : this.functionNeighborhood.keySet()) {
			// Skip the the otherId
			if (fID == otherId) {
				continue;
			}
			
			for (int x = 0; x < domainPower; x++) {
				
				// Calc local Qs using local Rs from prev round
				String myRKey = rKey(round - 1, agentIndex, fID, agentIndex, x);
				BigInteger myR = this.variabler.Get(myRKey);
				if (myR == null) {
					debug(true, "ERROR: Unable to find my R key:" + myRKey);					
				}
				localQs[x] = localQs[x].add(myR).mod(p);
				
				// Calc remote Qs				
				String otherRKey = rKey(round - 1, fID, fID, agentIndex, x);
				BigInteger otherR = this.variabler.Get(otherRKey);
				if (otherR == null) {
					debug(true, "ERROR: Unable to find otherR key:" + otherRKey);					
				}
				Ws[x] = Ws[x].multiply(otherR).mod(p);				
			}
		}
		
		
		// Store local Qs and create an encrypted version of them, it will be used for protocol 2 line 1.
		BigInteger encLocalQs[] = new BigInteger[domainPower];
		for (int x = 0; x < domainPower; x++) {
			variabler.Set(qKey(round, agentIndex, agentIndex, otherId, x), localQs[x]);
			encLocalQs[x] = paillierMgr.Encryption(ePaillierKey(this.agentIndex), localQs[x]);
		}
		
		// This message contains the two parts of the Qs variable
		//   Local, which is encrypted with Ei
		//   Remote, which is encrypted with Fi
		InjectQsMsg msg = new InjectQsMsg(round, agentIndex, otherId, encLocalQs, Ws);				 
		debug(debug, String.format("Sending Qs %s to %d", msg.toString(true), functionNode.ID)); 
		nodeService.sendMsgToNode(msg, functionNode);
	}

	
	private void processWsRequestMsgHandler(Node sender, ProcessWsRequestMsg msg) {
		// TODO add assert
		// msg.round == this.round - 1
		
		// ASSERT msg.Ws.length should be the size of the target id domain power
		BigInteger[] Ws = new BigInteger[msg.Ws.length]; 
		
		// Msg.Ws is a matrix, that is used to calculate The Rs of the next round.
		
		for (int i = 0; i < msg.Ws.length; i++) {
			// Find the min for W(i)
			BigInteger min = paillierMgr.Decryption(ePaillierKey(this.agentIndex), msg.Ws[i][0]);
			// msg.Ws[i].length should be domain power 
			for (int j = 1; j < msg.Ws[i].length; j++) {
				BigInteger tmpValue = paillierMgr.Decryption(ePaillierKey(this.agentIndex), msg.Ws[i][j]);
				min = min.min(tmpValue); // sorry didn't resist
			}
			// set the local share of the new R
			
			BigInteger r = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE)).mod(p);
			// Storing the R of the next round
			// Note that this is stored as is, no need to encrypt it 			
			String myRKey = rKey(msg.round, agentIndex, msg.source, agentIndex, i);
			debug(debug, "Storing myR: " + myRKey + ": " + r);
			variabler.Set(myRKey, r);

			Ws[i] = min.subtract(r).mod(p);			
		}
		
		// int round, int affinity, int source, int target, BigInteger[] s
		ProcessWsRespondMsg respondMsg = new ProcessWsRespondMsg(msg.round, msg.source, msg.source, msg.target, Ws, msg.opaque);
		debug(debug, "Sending respondMsg to " + respondMsg.toString(true));
		nodeService.sendMsgToNode(respondMsg, sender);
		
		
	}
	
	
	private void kickStartLastRound() {
		debug(debug, "Last round");
		
		BigInteger shifter = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE)).mod(p);
		BigInteger encShifter = paillierMgr.Encryption(ePaillierKey(this.agentIndex), shifter);
		
		BigInteger[] Ms = new BigInteger[domainPower];
		for (int x = 0; x < domainPower; x++) {
			Ms[x] = BigInteger.ONE;
		}

		
		int functionID = -1;
		// Iterate over all function neighborhood
		for (Integer fID : this.functionNeighborhood.keySet()) {
			functionID = fID;
			for (int x = 0; x < domainPower; x++) {				
				String myRKey = rKey(round - 1, agentIndex, fID, agentIndex, x);
				BigInteger myR = this.variabler.Get(myRKey);
				if (myR == null) {
					debug(true, "ERROR: Unable to find local R key:" + myRKey);					
				}				
				String otherRKey = rKey(round - 1, fID, fID, agentIndex, x);
				BigInteger otherR = this.variabler.Get(otherRKey);
				if (otherR == null) {
					debug(true, "ERROR: Unable to find ^othere^ R key:" + otherRKey);					
				}
				BigInteger enclocalR = paillierMgr.Encryption(fPaillierKey(this.agentIndex), myR);
				Ms[x] = Ms[x].multiply(enclocalR).mod(p).multiply(otherR).mod(p);		
			}
		}
		
		for (int x = 0; x < Ms.length; x++) {
			Ms[x] = Ms[x].multiply(encShifter).mod(p);
		}
		// TODO P1 permutate Ms
		MinIndexReqMsg msg = new MinIndexReqMsg(this.agentIndex, Ms);
		debug(debug, "Sending MinIndexReqMsg to " + msg.toString());
		nodeService.sendMsgToNode(msg, functionNeighborhood.get(functionID));
	}
	

	private void minIndexRespondMsgHandler(Node sender, MinIndexRespondMsg msg) {
		xIndex = msg.index;
		debug(debug, "xIndex" + xIndex);
		runningStatus = false;
	}
	
	public int xIndex() {
		return xIndex;
	}
	
	public void init() {
		// TODO Auto-generated method stub
		
	}

	public boolean doneStatus() {
		// TODO Auto-generated method stub
		return !runningStatus;
	}
	
	
	public int getInternal(String key) {
		return round;
	}

	
	public void logState() {
		boolean debug = false;
		debug(debug, "State of Agnet "+ agentIndex + "(ID: " + nodeService.ID() + ")");
		debug(debug, "\tDomain power "+ domainPower);
		debug(debug, "\tRound ("+ round + "/" + lastRound + ")");
		debug(debug, "\tFunction Neighborhood:");
		// Send a Q message for each neighbor function node
		for (Integer otherId : this.functionNeighborhood.keySet()) {
			debug(debug, "\t\tWith " + otherId + " by " + functionNeighborhood.get(otherId).ID);
		}
	}

	public int assignment() {
		// TODO Auto-generated method stub
		return xIndex;
	}
}
