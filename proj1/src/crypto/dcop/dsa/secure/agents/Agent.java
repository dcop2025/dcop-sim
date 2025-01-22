package crypto.dcop.dsa.secure.agents;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import common.framework.nodes.MessageHandler;
import crypto.dcop.DcopAgent;
import crypto.dcop.dsa.secure.messages.ConstrainsRowSharingMsg;
import crypto.dcop.dsa.secure.messages.FinishRoundMsg;
import crypto.dcop.dsa.secure.messages.InjectSecretMsg;
import crypto.dcop.dsa.secure.messages.InjectSecretRespondMsg;
import crypto.dcop.dsa.secure.messages.PostBetaCompareMsg;
import crypto.dcop.dsa.secure.messages.ProcessGammaDeltaMultiplyMsg;
import crypto.dcop.dsa.secure.messages.ProcessGammaDeltaMultiplyResultMsg;
import crypto.dcop.dsa.secure.messages.ValueReadyMsg;
import crypto.dcop.dsa.secure.sequences.BitwiseComapreSeq;
import crypto.dcop.dsa.secure.sequences.SecureCompareSeq;
import crypto.dcop.dsa.secure.sequences.SubSharedsSeq;
import crypto.dcop.dsa.secure.sequences.CompareTestSeq;
import crypto.dcop.dsa.secure.sequences.ISequence;
import crypto.dcop.dsa.secure.sequences.InjectSeq;
import crypto.dcop.dsa.secure.sequences.LSBComputeSeq;
import crypto.dcop.dsa.secure.sequences.MultiplySeq;
import crypto.dcop.dsa.secure.sequences.ResolvedSharedsSeq;
import crypto.dcop.dsa.secure.sequences.messages.BitwiseComapreRequestMsg;
import crypto.dcop.dsa.secure.sequences.messages.BitwiseComapreRespondMsg;
import crypto.dcop.dsa.secure.sequences.messages.BitwiseCompareMsg;
import crypto.dcop.dsa.secure.sequences.messages.BitwiseCompareReady;
import crypto.dcop.dsa.secure.sequences.messages.CalcLSBRequestMsg;
import crypto.dcop.dsa.secure.sequences.messages.ClacLSBRespondMsg;
import crypto.dcop.dsa.secure.sequences.messages.ContinueCmpSeqPostSubMsg;
import crypto.dcop.dsa.secure.sequences.messages.InjectMultiplyResultAckMsg;
import crypto.dcop.dsa.secure.sequences.messages.InjectMultiplyResultMsg;
import crypto.dcop.dsa.secure.sequences.messages.MultiplySharesRequestMsg;
import crypto.dcop.dsa.secure.sequences.messages.MultiplySharesRespondMsg;
import crypto.dcop.dsa.secure.sequences.messages.PostBWCDoneMsg;
import crypto.dcop.dsa.secure.sequences.messages.PostLSBComputeMsg;
import crypto.dcop.dsa.secure.sequences.messages.PostMultiLSBComputeMsg;
import crypto.dcop.dsa.secure.sequences.messages.PrepLSBComputeRequestMsg;
import crypto.dcop.dsa.secure.sequences.messages.PrepLSBComputeRespondMsg;
import crypto.dcop.dsa.secure.sequences.messages.SubSharedRequestMsg;
import crypto.dcop.dsa.secure.sequences.messages.SubSharedRespondMsg;
import crypto.dcop.dsa.secure.sequences.messages.test.InjectTestDoneMsg;
import crypto.dcop.dsa.secure.sequences.messages.test.PostCompareTestMsg;
import crypto.dcop.dsa.secure.sequences.messages.test.PostMultiTestMsg;
import crypto.dcop.dsa.secure.messages.SecureCompareRequestMsg;
import crypto.dcop.dsa.secure.messages.SecureCompareRespondMsg;
import crypto.dcop.dsa.secure.messages.SecureMultiplyRequestMsg;
import crypto.dcop.dsa.secure.messages.SecureMultiplyRespondMsg;
import crypto.dcop.dsa.secure.messages.ReconstructRequestMsg;
import crypto.dcop.dsa.secure.messages.ReconstructRespondMsg;
import crypto.dcop.dsa.secure.messages.RequestSharedMsg;
import crypto.dcop.dsa.secure.messages.RequestSharedRespondMsg;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import crypto.utils.OModMath;
import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;
import crypto.utils.shamir.VectorGenerater;


public class Agent extends DcopAgent {

	
	
	private Map<String, ISequence> sequences;
	
	// Tools
	private Random random;
	private boolean debug = false;
	private void debug(boolean flag, String format, Object... args) {
		Global.log.logln(flag, "ID:" + ID + " " + String.format(format, args));
		//Global.log.logln(flag, "ID:" + ID + " " + String.format(format, args));
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
	
		
	////////////////////////////
	// PDSA Section: Start
	
		
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
	static final String waiting_on_gammadelta = "waiting_on_gammadelta";
	static final String waiting_on_reconstruct = "waiting_on_reconstruct";
	static final String waiting_on_finish_round = "waiting_on_finish_round";

	// keys for counter
	static final String gen_shared_key = "gen_shared";
	static final String mcp_comapre_key = "mcp_comapre";
	static final String mcp_mult_key = "mcp_mult";
	static final String reconstruct_key = "reconstruct";

	private Map<String, Shared> sharedStorage;
	public Map<String, Integer> counters;
	private Map<String, Integer> waiters;	
	private boolean triggerPDSA;
	private boolean runningPDSA;
	private int shamirThreshold;
	private double roundThreshold;
	private long prime;
	
	private int round;
	private int domainPower;
	private int xIndex; 
	private long seed;
	
	private boolean xpdebug;
	
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

		registerMsgHandlers(InjectSecretMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				injectSecretMsgHandler(sender, (InjectSecretMsg) msg);
			}
		});

		registerMsgHandlers(InjectSecretRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				injectSecretRespondMsgHandler(sender, (InjectSecretRespondMsg) msg);
			}
		});

		registerMsgHandlers(InjectTestDoneMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				injectTestDoneMsgHandler(sender, (InjectTestDoneMsg) msg);
			}
		});

		
		
		registerMsgHandlers(RequestSharedMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				requestSharedMsgHandler(sender, (RequestSharedMsg) msg);
			}
		});

		registerMsgHandlers(RequestSharedRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				requestSharedRespondMsgHandler(sender, (RequestSharedRespondMsg) msg);
			}
		});
		
		registerMsgHandlers(ConstrainsRowSharingMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				constrainsRowSharingMsgHandler(sender, (ConstrainsRowSharingMsg) msg);
			}
		});

		registerMsgHandlers(ValueReadyMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				valueReadyMsgHandler(sender, (ValueReadyMsg) msg);
			}
		});
				
		registerMsgHandlers(SecureCompareRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				secureCompareRequestMsgHandler(sender, (SecureCompareRequestMsg) msg);
			}
		});

		registerMsgHandlers(SecureCompareRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				secureCompareRespondMsgHandler(sender, (SecureCompareRespondMsg) msg);
			}
		});

		registerMsgHandlers(SecureMultiplyRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				secureMultiplyRequestMsgHandler(sender, (SecureMultiplyRequestMsg) msg);
			}
		});

		registerMsgHandlers(SecureMultiplyRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				secureMultiplyRespondMsgHandler(sender, (SecureMultiplyRespondMsg) msg);
			}
		});

		registerMsgHandlers(ReconstructRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				reconstructRequestMsgHandler(sender, (ReconstructRequestMsg) msg);
			}
		});
			
		registerMsgHandlers(ReconstructRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				reconstructRespondMsgHandler(sender, (ReconstructRespondMsg) msg);
			}
		});

		registerMsgHandlers(FinishRoundMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				finishRoundMsgMsgHandler(sender, (FinishRoundMsg) msg);
			}
		});

		registerMsgHandlers(ProcessGammaDeltaMultiplyMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				processGammaDeltaMultiplyMsgHandler(sender, (ProcessGammaDeltaMultiplyMsg) msg);
			}
		});

		registerMsgHandlers(ProcessGammaDeltaMultiplyResultMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				processGammaDeltaMultiplyResultMsgHandler(sender, (ProcessGammaDeltaMultiplyResultMsg) msg);
			}
		});
		
		registerMsgHandlers(MultiplySharesRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				multiplyObscurateMsgHandler(sender, (MultiplySharesRequestMsg) msg);
			}
		});
		
		registerMsgHandlers(MultiplySharesRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				multiplySharesRespondMsgHandler(sender, (MultiplySharesRespondMsg) msg);
			}
		});
			
		registerMsgHandlers(InjectMultiplyResultMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				injectMultiplyResultMsgHandler(sender, (InjectMultiplyResultMsg) msg);
			}
		});
		

		registerMsgHandlers(InjectMultiplyResultAckMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				injectMultiplyResultAckMsgHandler(sender, (InjectMultiplyResultAckMsg) msg);
			}
		});

		registerMsgHandlers(PostMultiTestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				postMultiTestMsgHandler(sender, (PostMultiTestMsg) msg);
			}
		});
		
		registerMsgHandlers(SubSharedRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				subSharedRequestMsgHandler(sender, (SubSharedRequestMsg) msg);
			}
		});

		
		registerMsgHandlers(ContinueCmpSeqPostSubMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				continueCmpSeqPostSubMsgHandler(sender, (ContinueCmpSeqPostSubMsg) msg);
			}
		});

		registerMsgHandlers(PrepLSBComputeRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				prepLSBComputeRequestMsgHandler(sender, (PrepLSBComputeRequestMsg) msg);
			}
		});

		registerMsgHandlers(SubSharedRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				subSharedRespondMsgHandler(sender, (SubSharedRespondMsg) msg);
			}
		});

		registerMsgHandlers(PrepLSBComputeRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				prepLSBComputeRespondMsgHandler(sender, (PrepLSBComputeRespondMsg) msg);
			}
		});

		registerMsgHandlers(BitwiseComapreRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				bitwiseComapreRequestMsgHandler(sender, (BitwiseComapreRequestMsg) msg);
			}
		});

		registerMsgHandlers(BitwiseComapreRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				BitwiseComapreRespondMsgHandler(sender, (BitwiseComapreRespondMsg) msg);
			}
		});

		registerMsgHandlers(PostBWCDoneMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				PostBWCDoneMsgHandler(sender, (PostBWCDoneMsg) msg);
			}
		});

		registerMsgHandlers(PostMultiLSBComputeMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				PostMultiLSBComputeMsgHandler(sender, (PostMultiLSBComputeMsg) msg);
			}
		});

		registerMsgHandlers(CalcLSBRequestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				CalcLSBRequestMsgHandler(sender, (CalcLSBRequestMsg) msg);
			}
		});

		registerMsgHandlers(ClacLSBRespondMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				ClacLSBRespondMsgHandler(sender, (ClacLSBRespondMsg) msg);
			}
		});
		
		registerMsgHandlers(PostLSBComputeMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				PostLSBComputeMsgHandler(sender, (PostLSBComputeMsg) msg);
			}
		});
		
		registerMsgHandlers(PostCompareTestMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				PostCompareTestMsgHandler(sender, (PostCompareTestMsg) msg);
			}
		});

		registerMsgHandlers(PostBetaCompareMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				PostBetaCompareMsgHandler(sender, (PostBetaCompareMsg) msg);
			}
		});
		
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

		
	public void TriggerPDSA(int round, double d) {
		triggerPDSA = true;
		round = round + 1;
	}
	
	public boolean RunningPDSA() {
		return runningPDSA;
	}
	
	// STATE: INIT
	private void kickStartPDSA() {
		debug(debug, "Kick start PDSA");
		shamirThreshold = (int) Math.floor((double)(neighbors.size()+1)/2);
		// Set running flags
		triggerPDSA = false;
		runningPDSA = true; 

		// Set random index -->#2
		xIndex = random.nextInt(domainPower);
		debug(debug, ">> index: " + xIndex);
		
		pdsaStartNewRound();		
	}
	
	// STATE: START ROUND
	private void pdsaStartNewRound() {
		debug(debug, "Starting new round " + round);
		round--;
		TickCounter("round", 1);
		// Clear last round
		sequences.clear();
		sharedStorage.clear();
		selfInject();
		
		if (round == 0) {
			pdsaEndOfRun();
			return;
		}
		sendSelectedVectorToEveryone();
	}
	
	// STATE: Done
	private void pdsaEndOfRun() {
		debug(true, "PDSA Done index is" + xIndex);
		runningPDSA = false;
	}
	
	// STATE: START ROUND
	private void sendSelectedVectorToEveryone() {
		for (Agent other: neighbors) {
			sendSelectedVectorToAgent(other);
		}
	}
	
	// STATE: START ROUND
	private void sendSelectedVectorToAgent(Agent other) {
		// extract the constrain into a vector
			
		int[][] constraints = this.constraints.get(other.ID);
		if (constraints == null) {
			// set vector to zero
		}
		int[] vector = Arrays.copyOf(constraints[xIndex], constraints[xIndex].length);

		// Create a secret generator
		VectorGenerater vGen = new VectorGenerater(vector, shamirThreshold, prime, random);
		
		// Can't use BroadcastMsg as each agnet gets a different msg
		// Send to all agent shares in the vector
		for (Agent agnet : neighbors) {
			Shared[] shareds = vGen.generate(agnet.ID);
			TickCounter(gen_shared_key, shareds.length);
			ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(ID, other.ID, round, shareds);
			send(msg, agnet); 
		}
		// Also send to yourself shares		
		Shared[] shareds = vGen.generate(ID);
		TickCounter(gen_shared_key, shareds.length);
		debug(debug, "Sending a shared to myself " +  Arrays.toString(shareds) + " on agent " + other.ID);
		ConstrainsRowSharingMsg msg = new ConstrainsRowSharingMsg(ID, other.ID, round, shareds);
		this.constrainsRowSharingMsgHandler(this, msg);	
	}
		
	private void constrainsRowSharingMsgHandler(Node other, ConstrainsRowSharingMsg msg) {
		debug(debug, "Got sherad vector on " + msg.agentB + " from " + msg.agentA);
		
		// Storing shared under the following key wb - agent id - index 
		String baseKey = wb_i_key + msg.agentB + "-";
			
		// Store each entry in the shared storage in accumulating manner  
		for (int i = 0; i < msg.shareds.length; i++) {
			String key = baseKey + i;
			Shared shared = sharedStorage.get(key);
			if (shared == null) {
				shared = msg.shareds[i];
			} else {
				shared = shared.add(msg.shareds[i], prime);
			}
			debug(debug, "adding key " + key + " value: " + shared.toString());
			sharedStorage.put(key, shared);
		}
				
		// Tick counter
		boolean done = TickWaiters(baseKey, neighbors.size());
		if (!done) {
			return;
		}
		
		// Once this agent has all shares for agent "b" it can notify agent b, that he is ready
		readyToAssistAgent(msg.agentB);
	}
	
	private void readyToAssistAgent(int agnetID)  {
		debug(debug, "Telling agent " + agnetID + " I'm ready to assist him");
		String baseKey = wb_i_key + agnetID + "-";
		
		// Pre load k_i and w_i with the value of the first entry
		// k_i = 0
		sharedStorage.put(ki_key + agnetID, new Shared(ID, 0, 0));
		// wi = wb_i(0)
		Shared w_i = sharedStorage.get(baseKey + "0");
		if (w_i == null) {
			// Log error
			debug(true, "ERROR: Didn't find wi(0) for Agent " + agnetID  + " in agent " + ID);
		}
		sharedStorage.put(wi_key + agnetID, w_i);
		

		// Once this agent has all shares for agent "b" it can notify agent b, that he is ready
		SendMsg(agnetID, new ValueReadyMsg());
	}
	
	private void valueReadyMsgHandler(Node other, ValueReadyMsg msg) {
		debug(debug, "Agent " + other.ID + " is ready to assist me");
		boolean done = TickWaiters(ready_on_wi_key, neighbors.size() + 1);
		if (!done) {
			return;			
		}
		kickStartEvaluateNewValue();
		
	}
 
	private void kickStartEvaluateNewValue() {
		boolean skipRound = (roundThreshold < random.nextFloat());
		if (skipRound) {
			// if skipping round, simply start a new round
			debug(debug, "skipping round");
			finishRound();
			return;
		}
		// Starting for 1, as 0 was already set as the default min
		evaluateNewValue(1);
	}
	
	
	private void evaluateNewValue(int u) { 		
		debug(debug, "Evaluating vs index: " + u);
		if (u == domainPower) {
			debug(debug, "Done evaluating, reconstructing");
			kickStartReconstruct();
			return;
		}
		// Send a compare request msg to everyone
		String rightKey = wb_i_key + ID + "-" + u;
		String leftKey = wi_key + ID;
		String resKey = beta_key + ID;		
		debug(debug, "broadcasting to compare for agent " + ID + " u: " + u);
		
		// start for debug
		Shared left = sharedStorage.get(leftKey);
		//Shared left = sharedStorage.get(rightKey);
		if (left == null) {
			// Log error
			debug(true, "ERROR: Didn't find " + leftKey + " in agent " + ID);
		} 
		Shared right = sharedStorage.get(rightKey);
		//Shared right = sharedStorage.get(leftKey);
		if (right == null) {
			// Log error
			debug(true, "ERROR: Didn't find " + rightKey + " in agent " + ID);
		} 
				
		/// >>>>>>
 
		int res = left.real() > right.real() ? 1 : 0;
		
		String cmpSeqKey = String.format("compare-beta-%d-%s", round, resKey);		
		//SecureCompareSeq compareSeq = new SecureCompareSeq(leftKey, rightKey, resKey, new PostBetaCompareMsg(cmpSeqKey, u, res));		
		PostBetaCompareMsg x = new PostBetaCompareMsg(cmpSeqKey, u, res);
				x.withShared(left, right);
		SecureCompareSeq compareSeq = new SecureCompareSeq(rightKey, leftKey, resKey, x);
		sequences.put(cmpSeqKey, compareSeq);
		
		// 2. Create a seq for calc a - b
		String subSeqKey = String.format("%s-sub", cmpSeqKey);
		//SubSharedsSeq subSeq = new SubSharedsSeq(leftKey, rightKey, subSeqKey, cmpSeqKey, new ContinueCmpSeqPostSubMsg(cmpSeqKey, subSeqKey));
		SubSharedsSeq subSeq = new SubSharedsSeq(rightKey, leftKey, subSeqKey, cmpSeqKey, new ContinueCmpSeqPostSubMsg(cmpSeqKey, subSeqKey));
		sequences.put(subSeqKey, subSeq);
		
		//SubSharedRequestMsg subSharedMsg = new SubSharedRequestMsg(leftKey, rightKey, subSeqKey, subSeqKey);
		SubSharedRequestMsg subSharedMsg = new SubSharedRequestMsg(rightKey, leftKey, subSeqKey, subSeqKey);
		BroadcastMsg(subSharedMsg);

	
//		BroadcastMsg(new SecureCompareRequestMsg(leftKey, rightKey, resKey, ID, u));
	}
	
	
	private void PostBetaCompareMsgHandler(Node sender, PostBetaCompareMsg msg) {
		debug(debug, "everyone done comparing, telling geveryone time to multiply index " + msg.u + "for me");
		// for debug
		String beta_i_key = beta_key + ID;
		Shared beta_i = sharedStorage.get(beta_i_key);
		if (beta_i == null) {
			debug(true, "ERROR: Didn't find " + beta_i_key + " in agent " + ID);
		}		
		if (msg.expected != beta_i.real()) {
			debug(true, String.format("ERROR: compare mismatch %d %d left %s right %s", msg.expected, beta_i.real(), msg.left, msg.rigth));
		} 		
		// for debug
		BroadcastMsg(new SecureMultiplyRequestMsg(ID, msg.u));
	}
	
	
	private void secureCompareRequestMsgHandler(Node sender, SecureCompareRequestMsg msg) {
		// TODO: Need to do same calc here to ensure that beta contains the right value
		// TODO write a value in beta
		Shared left = sharedStorage.get(msg.left);
		if (left == null) {
			// Log error
			debug(true, "ERROR: Didn't find " + msg.left + " in agent " + ID);
		} 
		Shared right = sharedStorage.get(msg.right);
		if (right == null) {
			// Log error
			debug(true, "ERROR: Didn't find " + msg.right + " in agent " + ID);
		} 

		TickCounter(mcp_comapre_key, 1);
				
		int res = left.real() > right.real() ? 1 : 0;
				
		sharedStorage.put(msg.result, new Shared(ID, res, res));

		debug(debug, "Compare for agent " + msg.ownerID + " left: " + left + " right: " + right + " res: " + res + " u: " + msg.opaque);
		// Also ack back that the compare result is ready
		SendMsg(msg.ownerID, new SecureCompareRespondMsg(msg.opaque));

	}
	
	private void secureCompareRespondMsgHandler(Node sender, SecureCompareRespondMsg msg) {
		boolean done = TickWaiters(waiting_on_compare, neighbors.size()+1);
		if (!done) {
			return;
		}
		debug(debug, "everyone done comparing, telling geveryone time to multiply index " + msg.opaque() + "for me");
		BroadcastMsg(new SecureMultiplyRequestMsg(ID, msg.opaque()));
	}


	private class ProcessGammasDeltasMultiplyHelper {
		public Shared[] gammas;
		public Shared[] deltas;
		public int head;
		
		ProcessGammasDeltasMultiplyHelper(int size) {
			this.gammas = new Shared[size];
			this.deltas = new Shared[size];
			head = 0;
		}
		
		public void AddShared(Shared gamma, Shared delta) {
			this.gammas[head] = gamma;
			this.deltas[head] = delta;
			head++;
		}
	}
	
	ProcessGammasDeltasMultiplyHelper gammaDeltasHelper;

	private void secureMultiplyRequestMsgHandler(Node sender, SecureMultiplyRequestMsg msg) {
		String wb_i_u_key =  wb_i_key + msg.ownerID + "-" + msg.targetIndex;
		debug(debug, "handling mutli " + wb_i_u_key);
		Shared wb_i_u = sharedStorage.get(wb_i_u_key);
		if (wb_i_u == null) {
			debug(true, "ERROR: Didn't find " +wb_i_u_key + " in agent " + ID);
		}
		// After reading wb_i_u, it will never be read again, so it a good time to free it
		debug(debug, "freeing " + wb_i_u_key);
		sharedStorage.remove(wb_i_u_key);

		String beta_i_key = beta_key + msg.ownerID;
		Shared beta_i = sharedStorage.get(beta_i_key);
		if (beta_i == null) {
			debug(true, "ERROR: Didn't find " + beta_i_key + " in agent " + ID);
		}
		
		String w_i_key =  wi_key + msg.ownerID;		
		Shared w_i = sharedStorage.get(w_i_key);
		if (w_i == null) {
			debug(true, "ERROR: Didn't find " + w_i_key + " in agent " + ID);
		}
				
		String k_i_key = ki_key + msg.ownerID;
		Shared k_i = sharedStorage.get(k_i_key);
		if (k_i == null) {
			debug(true, "ERROR: Didn't find " + k_i_key + " in agent " + ID);
		}
		debug(debug, String.format("11 reading k_i %s %d", k_i_key, k_i.real()));

		TickCounter(mcp_mult_key, 1);
		Shared diff =  wb_i_u.sub(w_i, prime);
		// gamma_2t = diff * beta
		Shared gamma_2t = new Shared(ID, OModMath.multiply(diff.share(), beta_i.share(), prime), OModMath.multiply(diff.real(), beta_i.real(), prime));
		
		TickCounter(mcp_mult_key, 1);
		Shared u = new Shared(ID, msg.targetIndex, msg.targetIndex);
		Shared indexDiff = u.sub(k_i, prime);
		// delta_2t = index diff * beta
		//Shared delta_2t = new Shared(ID, indexDiff.getShare().multiply(beta_i.getShare()).mod(prime), indexDiff.getReal() * beta_i.getReal());
		Shared delta_2t = new Shared(ID, OModMath.multiply(indexDiff.share(), beta_i.share(), prime), OModMath.multiply(indexDiff.real(), beta_i.real(), prime));
		
		debug(debug, String.format("gamma_2t is %s delta_2t is %s", gamma_2t.toString(), delta_2t.toString()));
		
		debug(debug, String.format(">>>> u real %d k_i real %d indexDiff real %d", u.real(), k_i.real(), indexDiff.real()));
		debug(debug, String.format(">>>> beta real %d delta_2t real %d", beta_i.real(), delta_2t.real()));
		
		// Now the agent holds a shared in the gamma_2t and delta_2t, which needs to be reduce to 1t,
		// for that add random value to the shared and send it to the owner agent
		Shared gamma_2t_tilda = gamma_2t.add(new Shared(ID, 1, 1), prime);
		Shared delta_2t_tilda = delta_2t.add(new Shared(ID, 1, 1), prime);
		debug(debug, String.format(">>>> delta_2t_tilda real %d ", delta_2t_tilda.real()));

		if (msg.ownerID == ID) {
			gammaDeltasHelper = new ProcessGammasDeltasMultiplyHelper(neighbors.size()+1);
		}
		
		ProcessGammaDeltaMultiplyMsg processMsg = new ProcessGammaDeltaMultiplyMsg(msg.ownerID, ID, msg.targetIndex, gamma_2t_tilda, delta_2t_tilda);
		SendMsg(msg.ownerID, processMsg);
		
		/*
		Shared gamma_v = new Shared(msg.index(), BigInteger.valueOf(gamma_v_real), gamma_v_real);
		String gamma_i_key = gamma_key + msg.ownerID();
		sharedStorage.put(gamma_i_key, gamma_v);
		
		TickCounter(mcp_mult_key, 1);
		int indexDiff = msg.index() - k_i.getReal();
		int delta_v_real = beta_i.getReal() * indexDiff;
		Shared delta_v = new Shared(msg.index(), BigInteger.valueOf(delta_v_real), delta_v_real);
		String delta_i_key = delta_key + msg.ownerID();
		sharedStorage.put(delta_i_key, delta_v);
				
		sharedStorage.put(w_i_key, w_i.add(gamma_v, prime));
		sharedStorage.put(k_i_key, k_i.add(delta_v, prime));
				
		debug(debug, "doing mutli for " + msg.ownerID() + " u: " + msg.index() + " wb_i_u: " + wb_i_u + " w_i: " + w_i + " beta_i" + beta_i + " k_i: " + k_i + " w_i: " + (w_i.getReal() + gamma_v.getReal()) + " k_i: " + (k_i.getReal() + delta_v.getReal()));

		// After finish with the multi send a done respond
		SendMsg(msg.ownerID(), new SecureMultiplyRespondMsg(msg.ownerID(), msg.index()));
		*/
	}
	
	private void processGammaDeltaMultiplyMsgHandler(Node sender, ProcessGammaDeltaMultiplyMsg msg) {
		// Store shareds
		gammaDeltasHelper.AddShared(msg.gamma_2t_tilda, msg.delta_2t_tilda);

		debug(debug, String.format(">>>> storing delta %d from %d", msg.delta_2t_tilda.real(), sender.ID));
		// Tick waiters		
		boolean done = TickWaiters(waiting_on_gammadelta, neighbors.size()+1);
		if (!done) {
			return;
		} 
		debug(debug, "done waiting on gamma-deltas");
		
		
		// reconstruct gamma and delta
		long gamma_2t_tilda = ShamirSharedGen.reconstruct(gammaDeltasHelper.gammas, prime);
		long delta_2t_tilda = ShamirSharedGen.reconstruct(gammaDeltasHelper.deltas, prime);
		//BigInteger delta_2t_tilda = ShamirSharedGen.reconstruct(Arrays.copyOfRange(gammaDeltasHelper.deltas, 0, shamirThreshold), prime);
		
		if (gamma_2t_tilda!= msg.gamma_2t_tilda.real()) {
			debug(true, String.format("ERROR: >>>> reconstruct gamma(%d) doesn't match the real(%d)", gamma_2t_tilda,  msg.gamma_2t_tilda.real()));			
		}

		debug(debug, String.format(">>>> reconstruct delta %d", delta_2t_tilda));
		if (delta_2t_tilda != msg.delta_2t_tilda.real()) {
			debug(true, String.format("ERROR: >>>> reconstruct delta(%d) doesn't match the real(%d)", delta_2t_tilda,  msg.delta_2t_tilda.real()));			
		}

		
		
		ProcessGammaDeltaMultiplyResultMsg resultMsg = new ProcessGammaDeltaMultiplyResultMsg(msg.ownerID, msg.targetIndex, gamma_2t_tilda, delta_2t_tilda);
		BroadcastMsg(resultMsg);
	}
	
	private void processGammaDeltaMultiplyResultMsgHandler(Node sender, ProcessGammaDeltaMultiplyResultMsg msg) {
		
		
		
		Shared gamma = new Shared(ID, OModMath.sub(msg.gamma, 1, prime), OModMath.sub(msg.gamma, 1, prime));
		Shared delta = new Shared(ID, OModMath.sub(msg.delta, 1, prime), OModMath.sub(msg.delta, 1, prime));

		debug(debug, String.format(">>>> the delta tilda I got %d", msg.delta));
		debug(debug, String.format(">>>> the delta tilda I endup with  %d", delta.real()));
		
		debug(debug, String.format("storing gamma as %s delta as %s", gamma.toString(), delta.toString()));

		String w_i_key =  wi_key + msg.ownerID();		
		Shared w_i = sharedStorage.get(w_i_key);
		if (w_i == null) {
			debug(true, "ERROR: Didn't find " + w_i_key + " in agent " + ID);
		}
				
		String k_i_key = ki_key + msg.ownerID();
		Shared k_i = sharedStorage.get(k_i_key);
		if (k_i == null) {
			debug(true, "ERROR: Didn't find " + k_i_key + " in agent " + ID);
		}

		debug(debug, String.format("33 k_i real %d delta real %d", k_i.real(), delta.real()));
		Shared finalK_i = k_i.add(delta, prime);
		debug(debug, String.format(">>> final ki %d", finalK_i.real()));
		
		sharedStorage.put(w_i_key, w_i.add(gamma, prime));
		sharedStorage.put(k_i_key, finalK_i);

		// After finish with the multi send a done respond
		SendMsg(msg.ownerID(), new SecureMultiplyRespondMsg(msg.ownerID, msg.targetIndex));
	}
	
	
	private void secureMultiplyRespondMsgHandler(Node sender, SecureMultiplyRespondMsg msg) {
		boolean done = TickWaiters(waiting_on_multi, neighbors.size()+1);
		if (!done) {
			return;
		} 
		int nextIndex = msg.targetIndex + 1;
		debug(debug, "done multiply, going to evaluate the next value " + nextIndex);
		evaluateNewValue(nextIndex);		
	}
		
	public void kickStartReconstruct() {
		BroadcastMsg(new ReconstructRequestMsg(ID, ki_key + ID));
	}
	
	private void reconstructRequestMsgHandler(Node sender, ReconstructRequestMsg msg) {
		SendMsg(msg.ownerID(), new ReconstructRespondMsg(msg.ownerID(), msg.key(), BigInteger.ZERO));
	}
	
	private void reconstructRespondMsgHandler(Node sender, ReconstructRespondMsg msg) {
		boolean done = TickWaiters(waiting_on_reconstruct, neighbors.size()+1);
		if (!done) {
			return;
		} 

		TickCounter(reconstruct_key, 1);
		String k_i_key = ki_key + ID;
		Shared k_i = sharedStorage.get(k_i_key);
		if (k_i == null) {
			debug(true, "ERROR: Didn't find " + k_i_key + " in agent " + ID);
		}
		debug(debug, String.format("22 reading k_i %s %d", k_i_key, k_i.real()));
		xIndex = (int) k_i.real();
		debug(debug, "reconstructing _index to " + xIndex + "(" + domainPower + ")");
		if (domainPower <= xIndex) {
			throw new RuntimeException("Something went wrong!");
		}
		
		
		finishRound();
	}
	
	private void finishRound() {
		BroadcastMsg(new FinishRoundMsg(ID, round));	
	}
	
	private void finishRoundMsgMsgHandler(Node sender, FinishRoundMsg msg) {
		debug(debug, "got finish round msg");
		boolean done = TickWaiters(waiting_on_finish_round, neighbors.size()+1);
		if (!done) {
			return;
		}
		debug(debug, "finish round, moving to the next");
		pdsaStartNewRound();		
	}

			
	// PDSA Section: End
	////////////////////////////
	
	public Agent(int agentID, int domainPower, int totalRound, double roundThreshold, long prime, long seed) {
		super(agentID);
		this.domainPower = domainPower;
		this.constraints = new HashMap<Integer, int[][]>();
		this.neighbors = new Vector<Agent>();
		this.round = 1000;
		this.roundThreshold = roundThreshold;
		this.prime = prime;
		this.runningPDSA = false;
		this.seed = seed;
		registerMsgHandlers();
		this.event = null;
		setup();
	}
	
	String rInjectKey;
	Shared rInject;
	Shared rInjectBits[];
	
	
	public void InjectShared(String key, Shared shared, Shared sbits[]) {
		rInjectKey = key;
		rInject = shared;
		rInjectBits = sbits;
		selfInject();
	}
	
	public void selfInject() {
		this.sharedStorage.put(rInjectKey, rInject);
		if (rInjectBits == null) {
			return;
		}
		for (int i = 0; i < rInjectBits.length; i++) {			
			this.sharedStorage.put(bitKey(rInjectKey, i), rInjectBits[i]);
		}
	}
		
	static private String bitKey(String key, int index) {
		return String.format("%s(%d)", key, index);
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
	public void postStep() {
		if (triggerPDSA) {
			kickStartPDSA();
		}
		
		if (this.event != null) {
			boolean ok;
			ok = checkInjectEvent(this.event);
			if (ok) {
				this.event = null;
				return;
			}
			
			ok = checkMultiplyEvent(this.event);
			if (ok) {
				this.event = null;
				return;
			}
			
			ok = checkCompareTestEvent(this.event);
			if (ok) {
				this.event = null;
				return;
			}
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
		triggerPDSA = true;
	}
	
	@Override
	public boolean doneStatus() {		
		if (xpdebug) {
			return false;
		}
		return !runningPDSA;
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

	String event; 
	
	@Override
	public void triggerEvent(String event) {
		this.event = event;
		/*
		boolean ok;
		ok = checkInjectEvent(event);
		if (ok) {
			return;
		}
		 */
	}
	
		
	private boolean checkInjectEvent(String event) {
		xpdebug = true;
		shamirThreshold = (int) Math.floor((double)(neighbors.size()+1)/2);
		
		String pattern = "^inject:([a-zA-Z]+):(\\d+)$";
		java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
		java.util.regex.Matcher matcher = regex.matcher(event);
		if (!(matcher.matches())) {
			return false;
		}
				
		String key = matcher.group(1);
		int secret = Integer.parseInt(matcher.group(2));
				
		Shared[] shared = ShamirSharedGen.generate(secret, shamirThreshold, neighbors.size()+1, prime, random);
				
		int i = 1;
		for (Agent agnet : neighbors) {			
			InjectSecretMsg msg = new InjectSecretMsg("", key, shared[i]);
			send(msg, agnet);
			i++;
		}
		
		// Also send to yourself shares		
		InjectSecretMsg msg = new InjectSecretMsg("", key, shared[0]);
		SendMsg(this.ID, msg);
		

		return true;
	}
		
	private void injectSecretMsgHandler(Node sender, InjectSecretMsg msg) {
		sharedStorage.put(msg.key, msg.shared);
		InjectSecretRespondMsg respondMsg = new InjectSecretRespondMsg(msg.seqKey, msg.key);
		SendMsg(sender.ID, respondMsg);
	}

	private void injectSecretRespondMsgHandler(Node sender, InjectSecretRespondMsg msg) {
		boolean done = TickWaiters("inject-done", neighbors.size()+1);
		if (!done) {
			return;
		} 
		
		if (msg.seqKey == "") {
			ResolvedSharedsSeq seq = new ResolvedSharedsSeq(neighbors.size()+1);
			sequences.put(msg.key, seq);		
			
			BroadcastMsg(new RequestSharedMsg(msg.key));			
		} else {
			InjectSeq seq = (InjectSeq) sequences.get(msg.seqKey);
			SendMsg(this.ID, seq.postMsg);
			sequences.remove(msg.seqKey);
		}
	}
	
	private void requestSharedMsgHandler(Node sender, RequestSharedMsg msg) {
		Shared shared =  sharedStorage.get(msg.key);
		RequestSharedRespondMsg respondMsg = new RequestSharedRespondMsg(msg.key, shared);
		SendMsg(sender.ID, respondMsg);
		
	}
	
	private void requestSharedRespondMsgHandler(Node sender, RequestSharedRespondMsg msg) {
		ResolvedSharedsSeq seq = (ResolvedSharedsSeq) sequences.get("reslove-" + msg.key);
		seq.shareds[seq.index] = msg.shared;
		seq.index++;
		if (seq.index != seq.counter) {
			return;
		}
		
		long value = ShamirSharedGen.reconstruct(seq.shareds, prime);
		debug(debug, String.format("value %d real %d", value, seq.shareds[0].real()));
	}
	
	
	private boolean checkMultiplyEvent(String event) {
		xpdebug = true;
		shamirThreshold = (int) Math.floor((double)(neighbors.size()+1)/2);
		
		String pattern = "^multiplytest:([a-zA-Z]+):(\\d+):([a-zA-Z]+):(\\d+):([a-zA-Z]+)$";
		java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
		java.util.regex.Matcher matcher = regex.matcher(event);
		if (!(matcher.matches())) {
			return false;
		}
		

		String aKey = matcher.group(1);
		int aValue = Integer.parseInt(matcher.group(2));
		String bKey = matcher.group(3);
		int bValue = Integer.parseInt(matcher.group(4));
		String cKey = matcher.group(5);
		String seqKey = "multiply-" + cKey;

		debug(debug, "doing multiply for %s(%d) %s(%s) %s", aKey, aValue, bKey, bValue, cKey);
		
		// Create a multiply seq with post to message PostMultiTestMsg
		MultiplySeq multiplySeq = new MultiplySeq(aKey, bKey, cKey, neighbors.size()+1, new PostMultiTestMsg(cKey));
		sequences.put(seqKey, multiplySeq);
		
		InjectSeq injectAseq = new InjectSeq(aKey, aValue, neighbors.size()+1, new InjectTestDoneMsg(seqKey, "multiply"));
		sequences.put("inject-"+ aKey, injectAseq);
		InjectSeq injectBseq = new InjectSeq(bKey, bValue, neighbors.size()+1, new InjectTestDoneMsg(seqKey, "multiply"));
		sequences.put("inject-"+ bKey, injectBseq);
		
		kickStartInject("inject-"+ aKey);
		kickStartInject("inject-"+ bKey);
		return true;
		/*
		
		MultiplySeq seq = new MultiplySeq(aKey, bKey, cKey, neighbors.size()+1, new PostMultiTestMsg(cKey));
		sequences.put(seqKey, seq);
		MultiplySharesRequestMsg msg = new MultiplySharesRequestMsg(ID, seqKey, aKey, bKey, cKey);
		BroadcastMsg(msg);
		
		return true;
		*/
	}
		
	
	private void kickStartInject(String seqKey) {
		InjectSeq seq = (InjectSeq) sequences.get(seqKey);
		
		int secret = seq.value;
		String key = seq.key;

		debug(debug, "injecting key %s(%d)", key, secret);
		Shared[] shared = ShamirSharedGen.generate(secret, shamirThreshold, neighbors.size()+1, prime, random);
		
		int i = 1;
		for (Agent agnet : neighbors) {			
			InjectSecretMsg msg = new InjectSecretMsg(seqKey, key, shared[i]);
			send(msg, agnet);
			i++;
		}
		
		// Also send to yourself shares		
		InjectSecretMsg msg = new InjectSecretMsg(seqKey, key, shared[0]);
		SendMsg(this.ID, msg);
	}
	
	
	
	private void injectTestDoneMsgHandler(Node sender, InjectTestDoneMsg msg) {
		if(msg.testType == "multiply") {
			continueTestingMultiply(sender, msg);
			return;
		}
		if(msg.testType == "compare") {
			continueTestingCompare(sender, msg);
			return;
		}
		
	}
	
	private void continueTestingMultiply(Node sender, InjectTestDoneMsg msg) {
		MultiplySeq seq = (MultiplySeq) sequences.get(msg.seqKey);
		debug(debug, "got ack on injecting for %s", msg.seqKey);
		seq.injected++;
		if (seq.injected != 2) {
			return;
		}
		
		debug(debug, "now really starting to mutluply %s", msg.seqKey);
		MultiplySharesRequestMsg multiplymsg = new MultiplySharesRequestMsg(ID, msg.seqKey, seq.aKey, seq.bKey, seq.cKey);
		BroadcastMsg(multiplymsg);				
	}

	private void continueTestingCompare(Node sender, InjectTestDoneMsg msg) {
		CompareTestSeq seq = (CompareTestSeq) sequences.get(msg.seqKey);
		debug(debug, "got ack on injecting for %s", msg.seqKey);
		seq.injected++;
		if (seq.injected != 2) {
			return;
		}
						
		debug(debug, "now really starting to compare %s", msg.seqKey);
		// 1. Create a seq for the whole operation
		String cmpSeqKey = "compare-" + seq.cKey;		
		SecureCompareSeq compareSeq = new SecureCompareSeq(seq.aKey, seq.bKey, seq.cKey, new PostCompareTestMsg(cmpSeqKey));		
		sequences.put(cmpSeqKey, compareSeq);
		
		// 2. Create a seq for calc a - b
		String subSeqKey = String.format("%s-sub", cmpSeqKey);
		SubSharedsSeq subSeq = new SubSharedsSeq(seq.aKey, seq.bKey, subSeqKey, cmpSeqKey, new ContinueCmpSeqPostSubMsg(cmpSeqKey, subSeqKey));
		sequences.put(subSeqKey, subSeq);
		
		SubSharedRequestMsg subSharedMsg = new SubSharedRequestMsg(seq.aKey, seq.bKey, subSeqKey, subSeqKey);
		BroadcastMsg(subSharedMsg);
	}

	private void PostCompareTestMsgHandler(Node sender, PostCompareTestMsg msg) {
		SecureCompareSeq compareSeq = (SecureCompareSeq) sequences.get(msg.seqKey);
		Shared cmpYYY = sharedStorage.get(compareSeq.resKey);
		debug(true, String.format("cmpYYY (%s) is %s", compareSeq.resKey, cmpYYY));
		//SendMsg(ID, compareSeq.postMsg);
	
	}
	
	
	private void subSharedRequestMsgHandler(Node sender, SubSharedRequestMsg msg) {
		Shared a = sharedStorage.get(msg.aKey);
		Shared b = sharedStorage.get(msg.bKey);

		Shared res = a.sub(b, prime);
		sharedStorage.put(msg.resKey, res);
		
		SubSharedRespondMsg respondMsg = new SubSharedRespondMsg(msg.subSeqKey);
		SendMsg(sender.ID, respondMsg);
	}
	
	private void subSharedRespondMsgHandler(Node sender, SubSharedRespondMsg msg) {
		SubSharedsSeq seq = (SubSharedsSeq) sequences.get(msg.seqKey);
		if (!seq.ackAndDone(neighbors.size()+1)) {
			return;
		}
		
		// run the post action
		SendMsg(this.ID, seq.postMsg);
		sequences.remove(msg.seqKey);
	}
	
	private void continueCmpSeqPostSubMsgHandler(Node sender, ContinueCmpSeqPostSubMsg msg) {
		SubSharedsSeq subSeq = (SubSharedsSeq) sequences.get(msg.subSeqKey);
		sequences.remove(msg.subSeqKey);
		
			
		String lsbCmpSeqKey = subSeq.resKey + "-lsb";
		String lsbInputKey = subSeq.resKey;
		String lsbResKey = lsbCmpSeqKey; 

		// LSBComputeSeq lsbCmpSeq = new LSBComputeSeq(lsbInputKey, lsbResKey, subSeq.cmpSeqKey , neighbors.size()+1, new PostLSBComputeMsg(lsbCmpSeqKey));
		// using the result 
		SecureCompareSeq compareSeq = (SecureCompareSeq) sequences.get(subSeq.cmpSeqKey);
		
		//LSBComputeSeq lsbCmpSeq = new LSBComputeSeq(lsbInputKey, lsbResKey, compareSeq.resKey , neighbors.size()+1, new PostLSBComputeMsg(lsbCmpSeqKey, subSeq.cmpSeqKey));
		LSBComputeSeq lsbCmpSeq = new LSBComputeSeq(lsbInputKey, compareSeq.resKey, subSeq.cmpSeqKey , neighbors.size()+1, new PostLSBComputeMsg(lsbCmpSeqKey, subSeq.cmpSeqKey));
		sequences.put(lsbCmpSeqKey, lsbCmpSeq);
		PrepLSBComputeRequestMsg prepMsg = new PrepLSBComputeRequestMsg(lsbInputKey, "r-key", lsbResKey);
		BroadcastMsg(prepMsg);
		
	}
	
	private void PostLSBComputeMsgHandler(Node sender, PostLSBComputeMsg msg) {
// ---------------------------->
		LSBComputeSeq lsbCmpSeq = ( LSBComputeSeq) sequences.get(msg.lsbCmpSeqKey);
		//Shared lsbOutput = sharedStorage.get(lsbCmpSeq.outputKey);
 		
		SecureCompareSeq compareSeq = (SecureCompareSeq) sequences.get(msg.cmpSeqKey);
		//sharedStorage.put(compareSeq.resKey, lsbOutput);
		
		SendMsg(ID, compareSeq.postMsg);
		sequences.remove(msg.lsbCmpSeqKey);

	}
	
	private void prepLSBComputeRequestMsgHandler(Node sender, PrepLSBComputeRequestMsg msg) {
		Shared a = sharedStorage.get(msg.inputKey);
		Shared r = sharedStorage.get(msg.rKey);

		// [[c]] = 2*[[a]] + [[r]]
		Shared res = a.add(a, prime).add(r, prime);
		PrepLSBComputeRespondMsg respondMsg = new PrepLSBComputeRespondMsg(res, msg.seqKey);
		SendMsg(sender.ID, respondMsg);		
	}
	
	
	private void prepLSBComputeRespondMsgHandler(Node sender, PrepLSBComputeRespondMsg msg) {
		String lsbComputeSeqKey = msg.seqKey; 
		LSBComputeSeq lsbCmpSeq = (LSBComputeSeq) sequences.get(msg.seqKey);
		boolean done = lsbCmpSeq.prepReady(msg.shared);
		if (!done) {
			return;
		}
		long c = lsbCmpSeq.reconstructPerp(prime);
		
		
		String bwcSeqKey = lsbComputeSeqKey + "-bwc";
		BitwiseComapreSeq bwcSeq = new BitwiseComapreSeq(c, "r-key", bwcSeqKey, neighbors.size()+1, lsbComputeSeqKey, new PostBWCDoneMsg(bwcSeqKey, lsbComputeSeqKey));
		sequences.put(bwcSeqKey, bwcSeq);
		
		BitwiseComapreRequestMsg requestMsg = new BitwiseComapreRequestMsg(ID, c, "r-key", bwcSeqKey, bwcSeq.index, 0, bwcSeqKey);
		BroadcastMsg(requestMsg);
	}
		
	private void bitwiseComapreRequestMsgHandler(Node sender, BitwiseComapreRequestMsg msg) {

		int a_index = (int) ((msg.a >> msg.index) & 1); // Extract the i-th bit
		Shared b_index = sharedStorage.get(bitKey(msg.bKey, msg.index));
		Shared c_index;
		Shared d_index;
		Shared e_index;
		Shared e;
		Shared ea;
		
		if (a_index == 0) {
			c_index = b_index;
		} else {
			c_index = new Shared(ID, 1, 1).sub(b_index, prime);
		}
		String c_indexKey = bitKey(msg.resKey+"-c", msg.index);
		sharedStorage.put(c_indexKey, c_index);
		
		if (msg.isFirst()) {
			d_index = c_index;
			e_index = d_index;
			e = e_index;
			ea = e_index.constMultiply(a_index, prime);
			
			// Store d0, it will b euse later for LSB computation
			sharedStorage.put(msg.resKey + "-d0", c_index);
			sharedStorage.put(bitKey(msg.resKey+"-d", msg.index), d_index);
			sharedStorage.put(msg.resKey+"-e", e);
			sharedStorage.put(msg.resKey+"-ea", ea);

			// on first index, there isn't any thing complicated to do, return
			BitwiseComapreRespondMsg respondMsg = new BitwiseComapreRespondMsg(msg.bwcSeqKey, msg.phase);
			SendMsg(sender.ID, respondMsg);
			return;
				
		} else {
			if (msg.phase == 0) {
				BitwiseComapreRespondMsg respondMsg = new BitwiseComapreRespondMsg(msg.bwcSeqKey, msg.phase);
				SendMsg(sender.ID, respondMsg);

				return;
			}
			
			if (msg.phase == 2) {
				String d_iP1Key = bitKey(msg.resKey+"-d", msg.index+1);
				Shared d_iP1 = sharedStorage.get(d_iP1Key);
			
				Shared ed = sharedStorage.get(msg.resKey+"-d_p1c");
				
				// d_i = d_i+1 + c_i - d_i+1 * c_i						
				//d_index = d_iP1.add(c_index, prime).sub(d_iP1.multiply(c_index, prime), prime);
				d_index =  d_iP1.add(c_index, prime).sub(ed, prime);
				// e_i = d_i - d_i+1
				e_index = d_index.sub(d_iP1, prime);
				// e = simga(e_i) --> e = e + e_i
				e = sharedStorage.get(msg.resKey+"-e");
				e = e.add(e_index, prime);
				// ea = simga(e_i * a_i)
				ea = sharedStorage.get(msg.resKey+"-ea");
				ea = ea.add(e_index.constMultiply(a_index, prime), prime);

				sharedStorage.put(bitKey(msg.resKey+"-d", msg.index), d_index);
				sharedStorage.put(msg.resKey+"-e", e);
				sharedStorage.put(msg.resKey+"-ea", ea);

				if (msg.isLast()) {
					Shared one_ea = new Shared(ID,1, 1).sub(ea, prime);
					sharedStorage.put(msg.resKey+"-1-ea", one_ea);
				}
				
				BitwiseComapreRespondMsg respondMsg = new BitwiseComapreRespondMsg(msg.bwcSeqKey, msg.phase);
				SendMsg(msg.ownerID, respondMsg);
				return;
			}
			debug(true, "ERROR: don't be here 1");
		}
		debug(true, "ERROR: don't be here 2");
	}
	
	
	private void BitwiseComapreRespondMsgHandler(Node sender, BitwiseComapreRespondMsg msg) {
		String bwcSeqKey = msg.seqKey;
		BitwiseComapreSeq bwcSeq = (BitwiseComapreSeq) sequences.get(bwcSeqKey);
		
		if (msg.phase == 1) {
			BitwiseComapreRequestMsg requestMsg = new BitwiseComapreRequestMsg(ID, bwcSeq.a, bwcSeq.bKey, bwcSeq.resKey, bwcSeq.index, 2, bwcSeqKey);
			BroadcastMsg(requestMsg);
			return;
		}
		
		if (msg.phase == 3) {
			// done this was the last index to compare, exec the post bwc compare function
			SendMsg(ID, bwcSeq.postMsg); /* PostBWCDoneMsg */
			//sequences.remove(bwcSeqKey);
			return;
		}

		
		boolean done = bwcSeq.tickAck();
		if (!done) {
			return;
		}
		bwcSeq.resetAckCounter();

		// the the first bit (LSB) continue to the rest of the bits
		if (bwcSeq.index == 30 ) {
			bwcSeq.index--;
			BitwiseComapreRequestMsg requestMsg = new BitwiseComapreRequestMsg(ID, bwcSeq.a, bwcSeq.bKey, bwcSeq.resKey, bwcSeq.index, 0, bwcSeqKey);
			BroadcastMsg(requestMsg);
			return;
		}
	
		
		if (msg.phase == 0) {
			// the responds was for setting c, let's set dc
			String c_indexKey = bitKey(bwcSeq.resKey+"-c", bwcSeq.index);
			String d_iP1Key = bitKey(bwcSeq.resKey+"-d", bwcSeq.index+1);				
			String bwcMultiSeqKey = String.format("%s-multi-%d", bwcSeqKey, bwcSeq.index);
			BitwiseComapreRespondMsg nextPhaseMsg = new BitwiseComapreRespondMsg(bwcSeqKey, 1);

			
			MultiplySeq multiplySeq = new MultiplySeq(d_iP1Key, c_indexKey, bwcSeq.resKey+"-d_p1c", neighbors.size()+1, nextPhaseMsg);
			sequences.put(bwcMultiSeqKey, multiplySeq);
			MultiplySharesRequestMsg multiplymsg = new MultiplySharesRequestMsg(ID, bwcMultiSeqKey, d_iP1Key, c_indexKey, bwcMultiSeqKey);
			BroadcastMsg(multiplymsg);
			return;
		}
				
		if (msg.phase == 2) {
			if (bwcSeq.index != 0) {
				bwcSeq.index--;
				BitwiseComapreRequestMsg requestMsg = new BitwiseComapreRequestMsg(ID, bwcSeq.a, bwcSeq.bKey, bwcSeq.resKey, bwcSeq.index, 0, bwcSeqKey);
				BroadcastMsg(requestMsg);
				return;
			}
			// we past the last bit
			
			// the responds was for setting c, let's set dc
			String eKey = bwcSeq.resKey+"-e";
			String one_eaKey = bwcSeq.resKey+"-1-ea";				
			String bwcMultiSeqKey = String.format("%s-multi-e(1-ea)", bwcSeqKey);
			
			
			BitwiseComapreRespondMsg nextPhaseMsg = new BitwiseComapreRespondMsg(bwcSeqKey, 3);

						
			MultiplySeq multiplySeq = new MultiplySeq(eKey, one_eaKey, bwcSeq.resKey, neighbors.size()+1, nextPhaseMsg);
			sequences.put(bwcMultiSeqKey, multiplySeq);
			MultiplySharesRequestMsg multiplymsg = new MultiplySharesRequestMsg(ID, bwcMultiSeqKey, eKey, one_eaKey, bwcSeq.resKey);
			BroadcastMsg(multiplymsg);
			return;			
			
		} 
		
		debug(true, "error: don't be here 3");
		
	}
	
	
	private void PostBWCDoneMsgHandler(Node sender, PostBWCDoneMsg msg) {
		String bwcSeqKey = msg.bwcSeqKey;
		BitwiseComapreSeq bwcSeq = (BitwiseComapreSeq) sequences.get(bwcSeqKey);
				
		String lsbMultiSeqKey = bwcSeq.lsbComputeSeqKey + "-multi";
				
		MultiplySeq multiplySeq = new MultiplySeq(bwcSeq.resKey, bwcSeq.resKey+"-d0", bwcSeq.resKey+"-ed0", neighbors.size()+1, new PostMultiLSBComputeMsg(lsbMultiSeqKey, bwcSeqKey));
		sequences.put(lsbMultiSeqKey, multiplySeq);
		MultiplySharesRequestMsg multiplymsg = new MultiplySharesRequestMsg(ID, lsbMultiSeqKey, bwcSeq.resKey, bwcSeq.resKey+"-d0", bwcSeq.resKey+"ed0");
		BroadcastMsg(multiplymsg);				
	}

	
	private void PostMultiLSBComputeMsgHandler(Node sender, PostMultiLSBComputeMsg msg) {
		MultiplySeq multiplySeq = (MultiplySeq) sequences.get(msg.lsbMultiSeqKey);
		BitwiseComapreSeq bwcSeq = (BitwiseComapreSeq) sequences.get(msg.bwcSeqKey);
		sequences.remove(msg.bwcSeqKey);
		
		LSBComputeSeq lsbCmpSeq = (LSBComputeSeq) sequences.get(bwcSeq.lsbComputeSeqKey);
		CalcLSBRequestMsg calcMsg = new CalcLSBRequestMsg(bwcSeq.lsbComputeSeqKey, bwcSeq.resKey, bwcSeq.resKey+"-d0", multiplySeq.cKey, lsbCmpSeq.outputKey);
		BroadcastMsg(calcMsg);
		
		
	}
	
	private void CalcLSBRequestMsgHandler(Node sender, CalcLSBRequestMsg msg) {
		Shared e = sharedStorage.get(msg.eKey);
		Shared d0 = sharedStorage.get(msg.d0Key);
		Shared ed0 = sharedStorage.get(msg.ed0Key);
		Shared two_ed0 = ed0.constMultiply(2, prime);
		
		Shared x = e.add(d0, prime).sub(two_ed0, prime); 
		sharedStorage.put(msg.resKey, x);
		ClacLSBRespondMsg respondMsg = new ClacLSBRespondMsg(msg.seqKey);
		SendMsg(sender.ID,  respondMsg);
	}
	
	private void ClacLSBRespondMsgHandler(Node sender, ClacLSBRespondMsg msg) {
		LSBComputeSeq lsbCmpSeq = (LSBComputeSeq) sequences.get(msg.seqKey);
		boolean done = lsbCmpSeq.calcLSBAck();
		if (!done) {
			return;
		}
		SendMsg(ID, lsbCmpSeq.postMsg);
		sequences.remove(msg.seqKey);
	}
	
	
	private void multiplyObscurateMsgHandler(Node sender, MultiplySharesRequestMsg msg) {
 		Shared a = sharedStorage.get(msg.aKey);
		Shared b = sharedStorage.get(msg.bKey);
		Shared c_2t = new Shared(ID, OModMath.multiply(a.share(), b.share(), prime) , OModMath.multiply(a.real(), b.real(), prime));
			
		Shared r;
		if (msg.rKey == null) {
			r = new Shared(ID, 1, 1);
		} else {
			r = sharedStorage.get(msg.rKey);
		}
		Shared c_2t_r = c_2t.add(r, prime);
		
		
		SendMsg(msg.ownerID, new MultiplySharesRespondMsg(msg.ownerID, msg.seqKey, c_2t_r));
		

	}
	
	private void multiplySharesRespondMsgHandler(Node sender, MultiplySharesRespondMsg msg) {
		MultiplySeq seq = (MultiplySeq) sequences.get(msg.seqKey);
		boolean done = seq.addShare(msg.c_2t_r);
		if (!done) {
			return;
		}

		long c_2t_r = seq.resloved(shamirThreshold, prime);
		// TODO send a message to everyone
		BroadcastMsg(new InjectMultiplyResultMsg(msg.ownerID, msg.seqKey, seq.cKey, c_2t_r));
	}
	
	private void injectMultiplyResultMsgHandler(Node sender, InjectMultiplyResultMsg msg) {
		Shared r;
		if (msg.rKey == null) {
			r = new Shared(ID, 1, 1);
		} else {
			r = sharedStorage.get(msg.rKey);
		}
		
		Shared c = new Shared(ID, OModMath.sub(msg.c_2t_r, r.share(), prime), OModMath.sub(msg.c_2t_r, r.real(), prime));
		sharedStorage.put(msg.cKey, c);
			
		SendMsg(msg.ownerID, new InjectMultiplyResultAckMsg(msg.seqKey));
	}
	
	private void injectMultiplyResultAckMsgHandler(Node sender, InjectMultiplyResultAckMsg msg) {
		MultiplySeq seq = (MultiplySeq) sequences.get(msg.seqKey);
		boolean done = seq.countAck();
		if (!done) {
			return;
		}
		SendMsg(this.ID, seq.postMsg);
		sequences.remove(msg.seqKey);
	}
	
	
	private void postMultiTestMsgHandler(Node sender, PostMultiTestMsg msg) {
		ResolvedSharedsSeq seq = new ResolvedSharedsSeq(neighbors.size()+1);
		sequences.put("reslove-" + msg.ckey, seq);
		BroadcastMsg(new RequestSharedMsg(msg.ckey));		

	}
	
	public void setup() {
		if (seed == 0) {
			random = new Random();
		} else {
			random = new Random(seed+1000+ID);	
		}
		
		sharedStorage = new HashMap<String, Shared>();
		waiters = new HashMap<String, Integer>();
		counters = new HashMap<String, Integer>();
		sequences = new HashMap<String, ISequence>();
		shamirThreshold = (int) Math.floor((double)(neighbors.size()+1)/2);
	}
	
	
	private boolean checkCompareTestEvent(String event) {
		xpdebug = true;
		shamirThreshold = (int) Math.floor((double)(neighbors.size()+1)/2);
		
		String pattern = "^comparingtets:([a-zA-Z]+):(\\d+):([a-zA-Z]+):(\\d+):([a-zA-Z]+)$";
		java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
		java.util.regex.Matcher matcher = regex.matcher(event);
		if (!(matcher.matches())) {
			return false;
		}
		

		String aKey = matcher.group(1);
		int aValue = Integer.parseInt(matcher.group(2));
		String bKey = matcher.group(3);
		int bValue = Integer.parseInt(matcher.group(4));
		String cKey = matcher.group(5);
		String seqKey = "compare-test" + cKey;

		debug(debug, "doing compare for %s(%d) %s(%s) %s", aKey, aValue, bKey, bValue, cKey);
		
		// Create a compare seq with post to message 
		CompareTestSeq compareSeq = new CompareTestSeq(aKey, bKey, cKey, null); // PC0 a post compare function);
		sequences.put(seqKey, compareSeq);
		
		InjectSeq injectAseq = new InjectSeq(aKey, aValue, neighbors.size()+1, new InjectTestDoneMsg(seqKey, "compare"));
		sequences.put("inject-"+ aKey, injectAseq);
		InjectSeq injectBseq = new InjectSeq(bKey, bValue, neighbors.size()+1, new InjectTestDoneMsg(seqKey, "compare"));
		sequences.put("inject-"+ bKey, injectBseq);
		
		kickStartInject("inject-"+ aKey);
		kickStartInject("inject-"+ bKey);
		return true;
	}
	
}
