package utils.dcopgen;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import utils.secrets.SecretVectorGenerator;

import java.util.Vector;

import common.framework.messages.OMessage;
import crypto.pdsa.messages.ReconstructRequestMsg;
import crypto.pdsa.messages.ReconstructRespondMsg;
import crypto.pdsa.messages.SecureCompareRespondMsg;
import crypto.pdsa.messages.SecureMultiplyRespondMsg;
import crypto.pdsa.messages.ConstrainsRowSharingMsg;
import crypto.pdsa.messages.SecureMultiplyRequestMsg;
import crypto.pdsa.messages.SecureCompareRequestMsg;
import projects.dcopproj.nodes.messages.NotifySelectedValueMsg;
import projects.dcopproj.nodes.messages.ValueReadyMsg;

import java.util.Random;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Agent extends Node{

	// Tools
	private Random _random;
	private boolean _debug;
	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, "ID:" + ID + " " + logStr);
	}
	
	// COP info
	private int _domainRange;
	private Map<Integer, int[][]> _constraints;
	private Vector<Agent> _neighbors;

	
	private void DebugCOPInfo(boolean xdebug) {
		debug(xdebug, "COP Info");
		for (int key : _constraints.keySet()) {
			Global.log.logln(xdebug, "Other ID:" + key);
			for (int i = 0; i < _constraints.get(key).length; i++) {
				Global.log.log(xdebug, "\t");
				for (int j = 0; j < _constraints.get(key)[i].length; j++) {
					Global.log.log(xdebug, " "+ _constraints.get(key)[i][j]);
				}
				Global.log.log(xdebug, "\n");
			}
		}
	}
	
	
	////////////////////////////
	// PDSA Section: Start
	
	
	private void RestartCounters() {
		
	}
	
		
	// keys for secrets
	static final String ki_key = "k-";
	static final String wi_key = "w-";
	static final String wb_i_key = "wb-";
	static final String beta_key = "beta-";
	static final String gamma_key = "gamma-";
	static final String delta_key = "delta-";

	// keys for waiters
	static final String ready_on_wi_key = "ready_on_wi-";
	static final String waiting_on_compare = "waiting_on_compare";
	static final String waiting_on_multi = "waiting_on_multi";
	static final String waiting_on_reconstruct = "waiting_on_reconstruct";

	// keys for counter
	static final String gen_shared_key = "gen_shared";
	static final String mcp_comapre_key = "mcp_comapre";
	static final String mcp_mult_key = "mcp_mult";
	static final String reconstruct_key = "reconstruct";

	
	private Map<String, Integer> _sharedStorage;
	public Map<String, Integer> _counters;
	private Map<String, Integer> _waiters;	
	private boolean _triggerPDSA;
	private boolean _runningPDSA;
	private int _t;
	
	private boolean TickWaiters(String key, int waitTil) {
		// Also ack back that the compare result is ready
		Integer wait = _waiters.get(key);
		if (wait == null) {
			wait = 0;
		} 
		debug(_debug, "Tick " + key + "  waiter: " + wait);
		wait++;
		_waiters.put(key, wait);
		if (wait != waitTil) {
			return false;			
		}
		_waiters.remove(key);
		return true;
	}

	private void TickCounter(String key, int added) {
		Integer count = _counters.get(key);
		if (count == null) {
			count = 0;
		} 
		count = count + added;
		_counters.put(key, count);
	}
			
	
	private void SendMsg(int agnetID, OMessage msg) {
		if (agnetID == ID) {
			// If the message is for me, no need to send it
			msg.action(this, this);
			return;
		}
		Agent a = getAgentByID(agnetID);		
		if (a == null) {
			// Log an error before panic
			debug(true, "ERROR: Agent " + agnetID + " wasn't found in agent " + ID);
		}
		send(msg, a);				
	}
	
	private void BroadcastMsg(OMessage msg) {
		for (Agent a: _neighbors) {
			send(msg, a);
		}
		// Also need to take part of the compare so sending the message to myself
		msg.action(this, this);
	}

		
	public void TriggerPDSA(int round, double d) {
		DebugCOPInfo(true);
		_p = d;
		
		_triggerPDSA = true;
		_round = round + 1;
		//_debug = (ID == 1);
		_debug = true;
	}
	
	public boolean RunningPDSA() {
		return _runningPDSA;
	}
	
	// STATE: INIT
	private void KickStartPDSA() {
		debug(_debug, "Kick start PDSA");
		// Set running flags
		_triggerPDSA = false;
		_runningPDSA = true;
		// Set i/s 
		_random = new Random();
		_sharedStorage = new HashMap<String, Integer>();
		_waiters = new HashMap<String, Integer>();
		_counters = new HashMap<String, Integer>();
		_t = _neighbors.size() / 2;
		RestartCounters();
		// Set random index -->#2
		_index = _random.nextInt(_domainRange);
		debug(true, ">> index: " + _index);
		
		PdsaStartNewRound();		
	}
	
	// STATE: START ROUND
	private void PdsaStartNewRound() {
		debug(_debug, "Starting new round " + _round);
		_round--;
		if (_round == 0) {
			PdsaEndOfRun();
			return;
		}
		SendSelectedVectorToEveryone();
	}
	
	// STATE: Done
	private void PdsaEndOfRun() {
		debug(true, "PDSA Done");
		_runningPDSA = false;
	}
	
	// STATE: START ROUND
	private void SendSelectedVectorToEveryone() {
		for (Agent other: _neighbors) {
			SendSelectedVectorToAgent(other);
		}
	}
	
	// STATE: START ROUND
	private void SendSelectedVectorToAgent(Agent other) {
		// extract the constrain into a vector
		int[] vector;
		
		int[][] constraints = _constraints.get(other.ID);
		if (constraints == null) {
			// set vector to zero
		}
		vector = new int[constraints[_index].length];
		System.arraycopy(constraints[_index], 0, vector, 0, constraints[_index].length);

		// Create a secret generator
		SecretVectorGenerator generator = new SecretVectorGenerator(vector, _t);
		// Can't use BroadcastMsg as each agnet gets a different msg
		// Send to all agent shares in the vector
		for (Agent agnet : _neighbors) {
			int[] shares = generator.GenerateVector();
			TickCounter(gen_shared_key, shares.length);
			ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(ID, other.ID, _round, shares);
			send(msg, agnet); 
		}
		// Also send to yourself shares		
		int[] shares = generator.GenerateVector();
		TickCounter(gen_shared_key, shares.length);
		debug(_debug, "Sending a shared to myself " +  Arrays.toString(shares) + " on agent " + other.ID);
		ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(ID, other.ID, _round, shares);
		this.HandleShareSecretVectorMsg(this, msg);	
	}
		
	
	public void HandleShareSecretVectorMsg(Node other, ConstrainsRowSharingMsg msg) {
		debug((_debug || (msg.b() == 1)), "Got sherad vector on " + msg.b() + " from " + msg.a());
		// TODO: Add an assertion: msg.shares.size == agent b domain range
		
		// Storing shared under the following key wb - agent id - index 
		String baseKey = wb_i_key + msg.b() + "-";
			
		// Store each entry in the shared storage in accumulating manner  
		for (int i = 0; i < msg.shares().length; i++) {
			String key = baseKey + i;
			Integer value = _sharedStorage.get(key);
			if (value == null) {
				value = 0;
			}
			value = value + msg.shares()[i];
			debug(_debug, "adding key " + key + " value: " + value);
			_sharedStorage.put(key, value);
		}
				
		// Tick counter
		boolean done = TickWaiters(baseKey, _neighbors.size());
		if (!done) {
			return;
		}
		
		// Once this agent has all shares for agent "b" it can notify agent b, that he is ready
		ReadyToAssistAgent(msg.b());
	}
	
	private void ReadyToAssistAgent(int agnetID)  {
		debug(_debug, "Telling agent " + agnetID + " I'm ready to assist him");
		String baseKey = wb_i_key + agnetID + "-";
		
		// Preload k_i and w_i with the value of the first entry
		// k_i = 0
		_sharedStorage.put(ki_key + agnetID, 0);
		// wi = wb_i(0)
		Integer w_i = _sharedStorage.get(baseKey + "0");
		if (w_i == null) {
			// Log error
			debug(true, "ERROR: Didn't find wi(0) for Agent " + agnetID  + " in agent " + ID);
		}
		_sharedStorage.put(wi_key + agnetID, w_i);
		

		// Once this agent has all shares for agent "b" it can notify agent b, that he is ready
		SendMsg(agnetID, new ValueReadyMsg());
	}
	
	public void HandleValueReadyMsg(Node other, ValueReadyMsg msg) {
		debug(_debug, "Agent " + other.ID + " is ready to assist me");
		boolean done = TickWaiters(ready_on_wi_key, _neighbors.size() + 1);
		if (!done) {
			return;			
		}
		KickStartEvaluateNewValue();
		
	}
	
	//private int _u;
	
	// TODO Handle the wait code to know we can EvaluateNewValue 
	private void KickStartEvaluateNewValue() {
		boolean skipRound = (_p < _random.nextFloat());
		if (skipRound) {
			// if skipping round, simply start a new round
			debug(_debug, "skipping round");
			PdsaStartNewRound();
			return;
		}
		//_u = 0;
		// Starting for 1, as 0 was already set as the default min
		EvaluateNewValue(1);
	}
	
	
	private void EvaluateNewValue(int u) { 		
		// Bump the u index
		//_u++;
		debug(_debug, "Evaluating index: " + u);
		if (u == _domainRange) {
			debug(_debug, "Done evaluating, reconstructing");
			KickStartReconstruct();
			return;
		}
		// Send a compare request msg to everyone
		//String leftKey = wb_i_key + ID + "-" + u;
		//String rightKey = wi_key + ID;
		String rightKey = wb_i_key + ID + "-" + u;
		String leftKey = wi_key + ID;
		String resKey = beta_key + ID;
		TickCounter(mcp_comapre_key, 1);
		debug(_debug, "broadcasing to compare for agent " + ID + " u: " + u);
		BroadcastMsg(new SecureCompareRequestMsg(leftKey, rightKey, resKey, ID, u));
	}
	
	public void HandleSecureCompareRequestMsg(Node sender, SecureCompareRequestMsg msg) {
		// TODO: Need to do same calc here to ensure that beta contains the right value
		// TODO write a value in beta
		Integer left = _sharedStorage.get(msg.Left());
		if (left == null) {
			// Log error
			debug(true, "ERROR: Didn't find " + msg.Left() + " in agent " + ID);
		} 
		Integer right = _sharedStorage.get(msg.Right());
		if (right == null) {
			// Log error
			debug(true, "ERROR: Didn't find " + msg.Right() + " in agent " + ID);
		} 

		int res = 0;
		if (left > right) {
			res = 1;
		}
		_sharedStorage.put(msg.Result(), res);
		debug((_debug || (msg.ownerID() == 1)), "Compare for agent " + msg.ownerID() + " left: " + left + " right: " + right + " res: " + res + " u: " + msg.opaque());
		// Also ack back that the compare result is ready
		SendMsg(msg.ownerID(), new SecureCompareRespondMsg(msg.opaque()));
	};
	
	public void HandleSecureCompareRespondMsg(Node sender, SecureCompareRespondMsg msg) {
		boolean done = TickWaiters(waiting_on_compare, _neighbors.size()+1);
		if (!done) {
			return;
		}
		debug(_debug, "everyone done comparing, time to multiply index " + msg.opaque());
		TickCounter(mcp_mult_key, 2);
		BroadcastMsg(new SecureMultiplyRequestMsg(ID, msg.opaque()));
	}
	
	public void HandleSecureMultiplyRequestMsg(Node sender, SecureMultiplyRequestMsg msg) {
		String wb_i_u_key =  wb_i_key + msg.ownerID() + "-" + msg.index();
		debug(true, "handling mutli " + wb_i_u_key);
		Integer wb_i_u = _sharedStorage.get(wb_i_u_key);
		if (wb_i_u == null) {
			debug(true, "ERROR: Didn't find " +wb_i_u_key + " in agent " + ID);
		}
		// After reading wb_i_u, it will never be read again, so it a good time to free it
		debug(true, "freeing " + wb_i_u_key);
		_sharedStorage.remove(wb_i_u_key);
		

		String w_i_key =  wi_key + msg.ownerID();		
		Integer w_i = _sharedStorage.get(w_i_key);
		if (w_i == null) {
			debug(true, "ERROR: Didn't find " + w_i_key + " in agent " + ID);
		}
		String beta_i_key = beta_key + msg.ownerID();
		Integer beta_i = _sharedStorage.get(beta_i_key);
		if (beta_i == null) {
			debug(true, "ERROR: Didn't find " + beta_i_key + " in agent " + ID);
		}
		
		String k_i_key = ki_key + msg.ownerID();
		Integer k_i = _sharedStorage.get(k_i_key);
		if (k_i == null) {
			debug(true, "ERROR: Didn't find " + k_i_key + " in agent " + ID);
		}
		
		int diff = wb_i_u - w_i;
		int gamma_v = beta_i * diff;		
		String gamma_i_key = gamma_key + msg.ownerID();
		_sharedStorage.put(gamma_i_key, gamma_v);
		
		int indexDiff = msg.index() - k_i;
		int delta_v = beta_i * indexDiff;
		String delta_i_key = delta_key + msg.ownerID();
		_sharedStorage.put(delta_i_key, delta_v);
		
		
		_sharedStorage.put(w_i_key, w_i + gamma_v);
		_sharedStorage.put(k_i_key, k_i + delta_v);
		
		
		debug(_debug, "doing mutli for " + msg.ownerID() + " u: " + msg.index() + " wb_i_u: " + wb_i_u + " w_i: " + w_i + " beta_i" + beta_i + " k_i: " + k_i + " w_i: " + (w_i + gamma_v) + " k_i: " + (k_i + delta_v));
		
		// After finish with the multi send a done respond
		SendMsg(msg.ownerID(), new SecureMultiplyRespondMsg(msg.ownerID(), msg.index()));
	}
	
	public void HandleSecureMultiplyRespondMsg(Node sender, SecureMultiplyRespondMsg msg) {
		boolean done = TickWaiters(waiting_on_multi, _neighbors.size()+1);
		if (!done) {
			return;
		} 
		debug(_debug, "done multi, going to the next round");
		EvaluateNewValue(msg.index() + 1);
	}
	
	public void KickStartReconstruct() {
		BroadcastMsg(new ReconstructRequestMsg(ID, ki_key + ID));
	}
	
	public void HandleReconstructRequestMsg(Node sender, ReconstructRequestMsg msg) {	
		SendMsg(msg.ownerID(), new ReconstructRespondMsg(msg.ownerID(), msg.key(), BigInteger.ZERO));
	}
	
	public void HandleReconstructRespondMsg(Node sender, ReconstructRespondMsg msg) {
		boolean done = TickWaiters(waiting_on_reconstruct, _neighbors.size()+1);
		if (!done) {
			return;
		} 

		TickCounter(reconstruct_key, 1);
		String k_i_key = ki_key + ID;
		Integer k_i = _sharedStorage.get(k_i_key);
		if (k_i == null) {
			debug(true, "ERROR: Didn't find " + k_i_key + " in agent " + ID);
		}
		_index = k_i;
		debug(_debug, "reconstructing _index to " + _index);
		
		PdsaStartNewRound();
	}
	
	
	// PDSA Section: End
	////////////////////////////
	
	////////////////////////////
	// DSA Section: Start
	private boolean _triggerDSA;
	private boolean _runningDSA;
	private double _p;
	private int _index;
	private int _round;
	private Map<Integer, Integer> _otherValues;
	
	public int Index() {
		return _index;
	}

	public void TriggerDSA(int round, double d) {
		_triggerDSA = true;
		_round = round;
		_p = d;
	}

	private void KickStartDSA() {
		debug(_debug, "Kick start DSA");
		DebugCOPInfo(true);
		// Init DSA state
		_triggerDSA = false;
		_runningDSA = true;
		_random = new Random();
		_otherValues = new HashMap<Integer, Integer>();
		// The first thing that needs to be done is to select a random value
		
		_index = _random.nextInt(_domainRange);
		debug(true, ">> index: " + _index);
		SendSelectValueToNeighbors(_index);
	}
	
	private void SendSelectValueToNeighbors(int index) {
		// Create a message
		NotifySelectedValueMsg msg = new NotifySelectedValueMsg(index);
		// TX that message
		for (Agent agnet : _neighbors) {
			send(msg, agnet);
		}		
	}
	
	public void HandleNotifySelectedValueMsg(Node other, NotifySelectedValueMsg msg) {
		_otherValues.put(other.ID, msg.Index());
		if (_otherValues.size() != _neighbors.size()) {
			return;			
		}
		HandleEndDSARound();
	}
	
	private void HandleEndDSARound() {
		// TODO handle error that index is -1
		_index = SelectMinIndex();	
				
		SendSelectValueToNeighbors(_index);
		_round--;
		if (_round == 0) {
			debug(true, "End of DSA: Index: " + _index);
			_runningDSA = false;
			return;
		}
		// This should be always cleared,
		_otherValues.clear();
	}
	
	private int SelectMinIndex() {
		boolean skipRound = (_p < _random.nextFloat());
		if (skipRound) {
			return _index;
		}

		int cost = Integer.MAX_VALUE;
		int newIndex = -1;
		int tmpCost;
		for (int i = 0; i < _domainRange; i++) {
			tmpCost = 0;
			for (int key : _otherValues.keySet()) {
				int keyCost = _constraints.get(key)[i][_otherValues.get(key)];
				tmpCost += keyCost;	
				debug(_debug, "key: " + key + " key-cost: " + key + " tmp cost: " + tmpCost);
			}
			if (tmpCost < cost) {
				cost = tmpCost;
				newIndex = i;
			}
		}
		return newIndex;
	}
	
	public boolean RunningDSA() {
		return _runningDSA;
	}

	// DSA Section: End
	//////////////////////////////
	
	public Agent(int domainRange) {
		_domainRange = domainRange;
		_constraints = new HashMap<Integer, int[][]>();
		_neighbors = new Vector<Agent>();
		_triggerDSA = false;
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
	}

	@Override
	public void postStep() {
		if (_triggerDSA) {
			KickStartDSA();
			return;
		}
		if (_triggerPDSA) {
			KickStartPDSA();
		}
	}
	
	public int DomainRange() {
		return _domainRange;
	}

	// Add a constraints matrix with another agetn
	public void AddConnectionConstraints(Agent other, int[][] constraints) {
		_constraints.put(other.ID, constraints);
		_neighbors.add(other);
		// only one agent needs to add a bi connection, let it be the agent with higher id  
		if (ID > other.ID) {
			addBidirectionalConnectionTo(other);
		}
	}
		
	//
	public int GetConstraint(int otherID, int index, int otherIndex) {
		if (!_constraints.containsKey(otherID)) {
			return 0;
		}
		
		return _constraints.get(otherID)[index][otherIndex];		
	}

	private Agent getAgentByID(int otherID) {
		Agent other = null;		
		for (Agent a: _neighbors) {
			if (a.ID == otherID) {
				other = a;
			}
		}
		return other;
	}

}
