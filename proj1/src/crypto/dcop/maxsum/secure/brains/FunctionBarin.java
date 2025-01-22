package crypto.dcop.maxsum.secure.brains;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import common.framework.nodes.MessageHandler;
import common.framework.nodes.NodeService;
import common.framework.nodes.ONodeAlgo;
import common.framework.nodes.Variabler;
import crypto.dcop.DcopAgent;
import crypto.dcop.Problem.ConstraintsMatrix;
import crypto.dcop.maxsum.secure.messages.InjectQsMsg;
import crypto.dcop.maxsum.secure.messages.InjectRsMsg;
import crypto.dcop.maxsum.secure.messages.MinIndexReqMsg;
import crypto.dcop.maxsum.secure.messages.MinIndexRespondMsg;
import crypto.dcop.maxsum.secure.messages.ProcessWsRequestMsg;
import crypto.dcop.maxsum.secure.messages.ProcessWsRespondMsg;
import crypto.utils.PaillierMgr;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class FunctionBarin implements ONodeAlgo {

	// TODO P2: move this to a base brain 
	private boolean debug;
	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, String.format("ID(%d,%d): %s", constraints.a, constraints.b, logStr));
	}
	
	static final String r_key = "R";
	private String rKey(int round, int affinity, int source, int target, int index) {
		return String.format("%s^%d,%d_%d-->%d(%d)", r_key, round, affinity, source, target, index);
	}

	static final String q_key = "Q";
	private String qKey(int round, int affinity, int source, int target, int index) {
		return String.format("%s^%d,%d_%d-->%d(%d)", q_key, round, affinity, source, target, index);
	}

	static final String e_paillier_key = "E";
	private String ePaillierKey(int index) {
		return e_paillier_key + "-" + index;
	}

	static final String f_paillier_key = "F";
	private String fPaillierKey(int index) {
		return f_paillier_key + "-" + index;
	}

	
	private int ID;
	private Variabler variabler;
	private NodeService nodeService;

	private Random random;
	private PaillierMgr paillierMgr;
	
	private BigInteger p;

	ConstraintsMatrix constraints;
	private Map<Integer, DcopAgent> agnets;

	public FunctionBarin(NodeService nodeService, Variabler variabler, DcopAgent agentA, DcopAgent agentB, ConstraintsMatrix constraints, PaillierMgr paillierMgr, BigInteger p) {
		this.variabler = variabler;
		this.nodeService = nodeService;
		this.constraints = constraints;
		this.agnets = new HashMap<Integer, DcopAgent>();
		agnets.put(constraints.a, agentA); // TODO P2 there should be a better way to wrap constraints and agents 
		agnets.put(constraints.b, agentB);
		this.ID = this.nodeService.ID();
		this.p = p;
		this.paillierMgr = paillierMgr;
	}

	public void registerMsgHandlers() {
		nodeService.registerMsgHandlers(InjectQsMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				injectQsMsgHandler(sender, (InjectQsMsg) msg);
			}
		});		

		nodeService.registerMsgHandlers(ProcessWsRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				processWsRespondMsgHandler(sender, (ProcessWsRespondMsg) msg);
			}
		});		

		nodeService.registerMsgHandlers(MinIndexReqMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				minIndexReqMsgHandler(sender, (MinIndexReqMsg) msg);
			}
		});		
		
	}
	
	public void init() {
		// clear out the variable storage
		variabler.clear();
	}

	public void start() {
		// Upon start, setup the local and remote Rs by reseting them to zero
		random = new Random();
		
		// Setup Rs init value for agent a and agent b
		for (Integer agentId : this.agnets.keySet()) {
			// TODO P2 this is wrong, the function should get a single input, not take info on the agent from 2 different sources
			setRsForAgnet(agentId, 0);
		}		
	}
	

	private void setRsForAgnet(int agnetId, int round) {
		DcopAgent agent = this.agnets.get(agnetId);
		int domainPower = constraints.getPowerDomain(agnetId);
		BigInteger[] remoteRs = new BigInteger[domainPower];
		BigInteger[] encLocalRs = new BigInteger[domainPower];
		
		for (int x = 0; x < domainPower; x++) {
			// Set local R, also encrypt it, as send it to the agent
			BigInteger localR = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE)).mod(p);			
			variabler.Set(rKey(round, ID, ID, agnetId, x), localR);
			encLocalRs[x] = paillierMgr.Encryption(fPaillierKey(agnetId), localR);
			// Set Remote R
			remoteRs[x] = p.subtract(localR).mod(p);
			debug(false, String.format("Setting R: round: %d otherID: %d x: %d local %s remote %s", round, agnetId, x, localR.toString(), remoteRs[x].toString())); 
		}
		InjectRsMsg remoteMsg = new InjectRsMsg(
				round,
				constraints.getOtherId(agnetId),
				agnetId,
				encLocalRs,
				remoteRs,
				true);
		nodeService.sendMsgToNode(remoteMsg, agent);
	}
	
	private void injectQsMsgHandler(Node sender, InjectQsMsg msg) {
		// NO ASSERT msg.round
		// ASSERT msg.source == sender.ID & sender.ID is agnet a or agent b
		// ASSERT msg.target == this.ID
		// ASSERT len(msg.r) == domain power of  agnet a or agent b

		debug(debug, String.format("handling injectQsMsgHandler msg %s", msg.toString(true)));
		
		for (int x = 0; x < msg.local.length; x++) {
			// Store local Q's as is (note they are encrypted with Ei) 
			variabler.Set(qKey(msg.round, msg.source, msg.source, msg.target, x), msg.local[x]);
		}
		for (int x = 0; x < msg.remote.length; x++) {
			// Store remote Q's but decrypte them 
			BigInteger remoteQ = paillierMgr.Decryption(fPaillierKey(msg.source), msg.remote[x]);
			variabler.Set(qKey(msg.round, msg.target, msg.source, msg.target, x), remoteQ);
		}

		// With both part of Q_i, we can start protocol 2 to calc R of the other agent
		int otherId = getOtherId(msg.source);
		// Start protocol 2 with the other agent
		
		kickStartProtocol2(otherId, msg.source, msg.round); 
	}
	
	
	private void kickStartProtocol2(int targetId, int otherId, int round) {
		debug(debug, String.format("starting protocol 2 with %d (other id %d) round %d", targetId, otherId, round));
		
		// There are two agent active here.		
		//  * Target agent: is the agent that we are calculating the R for
		//  * Other agent: is the agent we are using his Qs to calculate the R
		
		// Since the value of Q is split between local and remote, and the remove is encrypted
		// We build a matrix for Ws, where:
		//  * Each row is for a different entry in the target domain
		//  * Each cell is for a different entry in the other domain		
		//  * The content of each cell is = Q_local(col) + C(row, col) + Q_remote(col) + factor
		//    but because Q_local is encrypted the calcuation is done by 
		//    Ei(facotr.add(localQ).add(BigInteger.valueOf(constrain)).mod(p)) * Q_remote(col)
		
		int domainPower = constraints.getPowerDomain(targetId);
		int otherDomainPower = constraints.getPowerDomain(otherId);

		BigInteger shifter = BigInteger.valueOf(random.nextInt(Integer.MAX_VALUE)).mod(p);
		debug(debug, String.format("domainPower %d other domain power %d shifter %s" , domainPower, otherDomainPower, shifter));
		
		// Running Protocol 2
		BigInteger Ws[][] = new BigInteger[domainPower][otherDomainPower];
				
		for (int x = 0; x < domainPower; x++) {
			for (int y = 0; y < otherDomainPower; y++) {
				BigInteger myQ = variabler.Get(qKey(round, targetId, otherId, targetId, y));
				if (myQ == null) {
					debug(true, "ERROR: didn't found local Q key %s");
				}
				
				BigInteger otherQ = variabler.Get(qKey(round, otherId, otherId, targetId, y));
				if (otherQ == null) {
					debug(true, "ERROR: didn't found remote Q key %s");
				}

				// change the x with the y as we are working for the other node right now
				int constrain = constraints.getConstraint(targetId, x,y);
				BigInteger tempW = shifter.add(myQ).mod(p).add(BigInteger.valueOf(constrain)).mod(p);
				tempW = paillierMgr.Encryption(ePaillierKey(otherId), tempW);
				Ws[x][y] = tempW.multiply(otherQ).mod(p);
			}
		}
		
		// TODO P0 run a permutation on Ws
		
		// Send the Ws to Back to Ai so it can return the min
		ProcessWsRequestMsg msg = new ProcessWsRequestMsg(round, otherId, targetId, Ws, shifter);
		Node otherAgent = agnets.get(targetId);
		debug(debug, String.format("Sending ProcessWsRequestMsg %s", msg.toString()));
		nodeService.sendMsgToNode(msg, otherAgent);		
	}
	
	private void processWsRespondMsgHandler(Node sender, ProcessWsRespondMsg msg) {
		// Asset msg.ws = power domain
		
		debug(debug, String.format("Handling ProcessWsRespondMsg %s", msg.toString(true)));
		
		BigInteger[] encLocalRs = new BigInteger[msg.s.length];
		
		 
		for (int x = 0; x < msg.s.length; x++) {
			// Store the r local r, by re-shifting the Ws (which calc by the agent) using the opaque.
			BigInteger localR =  msg.s[x].subtract(msg.opaque).mod(p);
			variabler.Set(rKey(msg.round, msg.affinity, msg.source, msg.target, x), localR);
			// Why encrypt using F_i? check the comment of 3 lines from here
			encLocalRs[x] = paillierMgr.Encryption(fPaillierKey(msg.target), localR);
		}
		
		// Send the the encrypted local R to the agent, as part of the first step of protocol 1/3
		InjectRsMsg remoteMsg = new InjectRsMsg(
				msg.round,
				msg.source,
				msg.target,
				encLocalRs,
				null,
				false);
		nodeService.sendMsgToNode(remoteMsg, sender);
	}
	
	private void minIndexReqMsgHandler(Node sender, MinIndexReqMsg msg) {
		int minIndex = 0;
		BigInteger minValue = paillierMgr.Decryption(fPaillierKey(msg.source), msg.m[0]);
		
		for (int x = 1; x < msg.m.length; x++) {
			BigInteger curValue = paillierMgr.Decryption(fPaillierKey(msg.source), msg.m[x]);
			minValue = minValue.min(curValue);
			if (minValue.equals(curValue)) {
				minIndex = x;
			}
		}
		
		MinIndexRespondMsg respMsg = new MinIndexRespondMsg(minIndex);
		nodeService.sendMsgToNode(respMsg, sender);
	}
	
	private int getOtherId(int id) {
		for (Integer agentID : this.agnets.keySet()) {
			if (agentID != id) {
				return agentID;
			} 			
		}		
		return -1;
	}

	public BigInteger calcRx(int x, BigInteger[] Qs, int[][] matrix, int jPowerDomain) {
		// Since BigInteger does have a max-big-integer, set the r value to the first as min
		int y = 0;
		// r = 0 + Qs[y] + matrix[x][y]
		BigInteger minR = BigInteger.ZERO.add(Qs[y]).add(BigInteger.valueOf(matrix[x][y]));
		for (y = 1; y < jPowerDomain; y++) {
			BigInteger tmp = BigInteger.ZERO.add(Qs[y]).add(BigInteger.valueOf(matrix[x][y]));
			// keep the min between r and tmp
			minR = tmp.min(minR);
		}
		return minR;
	}


	public boolean doneStatus() {
		// function node are passive
		return true;
	}
	
	public void logState() {
		boolean debug = false;
		debug(debug, "Log state for ID " + ID);		
		//debug(debug, "\ta range " + constraints.domainPowerAgentA() + " b range: " + constraints.domainPowerAgentB());
		debug(debug, String.format("\ta(%d) range %d. b(%d) range: %d", constraints.a, constraints.domainPowerAgentA(), constraints.b, constraints.domainPowerAgentB()));
		for (int i = 0; i < constraints.domainPowerAgentA(); i++) {
			String line = "\t[";
			for (int j = 0; j < constraints.domainPowerAgentB(); j++) {
				line = line + " " + constraints.matrix[i][j];
			}
			line = line + " ]";
			debug(debug, line);
		}
	}

	public int assignment() {
		// TODO Auto-generated method stub
		return -1;
	}
	
	public int getInternal(String key) {
		return 999;
	}

}