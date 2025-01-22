package crypto.dcop.maxsum.vanilla;

import java.math.BigInteger;

import common.framework.nodes.MessageHandler;
import common.framework.nodes.NodeService;
import common.framework.nodes.ONodeAlgo;
import common.framework.nodes.Variabler;
import crypto.dcop.DcopAgent;
import crypto.dcop.Problem.ConstraintsMatrix;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class AlgoFunction implements ONodeAlgo {

	private boolean debug;
	
	private void debug(boolean flag, String logStr) {
		Global.log.logln(flag, "ID:" + ID + " " + logStr);
	}
	
	private int ID;
	private Variabler variabler;
	private NodeService nodeService;

	ConstraintsMatrix constraints;
	private DcopAgent agentA;
	private DcopAgent agentB;

	public AlgoFunction(NodeService nodeService, Variabler variabler, DcopAgent agentA, DcopAgent agentB, ConstraintsMatrix constraints) {
		this.variabler = variabler;
		this.nodeService = nodeService;
		this.constraints = constraints;
		this.agentA = agentA;
		this.agentB = agentB;
		this.ID = this.nodeService.ID();
	}

	public void Start() {
		variabler.clear();	
	}

	public void UpdateQsMsgHandler(Node sender, UpdateQsMsg qMsg) {
		int[][] matrix;
		// TODO tie agent with range
		DcopAgent iAgent;
		DcopAgent jAgent;
		int iDomainRagne;
		int jDomainRagne;
		
		debug(debug, "Handleing a Qs update message from " + sender.ID + " Qs len:" + qMsg.qString());
		
		if (sender == this.agentA) {
			matrix = constraints.revMatrix();
			iAgent = this.agentB;
			iDomainRagne = constraints.domainPowerAgentB();					
			jAgent = this.agentA;
			jDomainRagne = constraints.domainPowerAgentA();			
		} else {
			matrix = constraints.matrix;
			iAgent = this.agentA;
			iDomainRagne = constraints.domainPowerAgentA();
			jAgent = this.agentB;
			jDomainRagne = constraints.domainPowerAgentB();
		}
	
		debug(debug, "iAgent " + iAgent.ID + "(" + iDomainRagne +")");
		debug(debug, "jAgent " + jAgent.ID + "(" + jDomainRagne +")");
		
		BigInteger[] Rs = new BigInteger[iDomainRagne];
		// Calc each entry in Rs
		for (int x = 0; x < iDomainRagne; x++) {
			Rs[x] = calcRx(x, qMsg.q(), matrix, jDomainRagne);
		}

		UpdateRsMsg rMsg = new UpdateRsMsg(jAgent.ID, qMsg.round()+1, Rs);
		debug(debug, "Sending Rs(" + rMsg.rString() + ") to " + iAgent.ID + " on node: " + jAgent.ID);
		nodeService.sendMsgToNode(rMsg, iAgent); 
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

	public void init() {
		// nothing to init
	}

	public void start() {
		// no action upon start
		
	}

	public boolean doneStatus() {
		// function node are passive
		return true;
	}
	
	public void registerMsgHandlers() {
		nodeService.registerMsgHandlers(UpdateQsMsg.class, new MessageHandler() {
			public void handle(Node sender, Message msg) {
				UpdateQsMsgHandler(sender, (UpdateQsMsg) msg);
			}
		});
	}

	public void logState() {
		boolean debug = false;
		debug(debug, "Log state");
		debug(debug, "\ta: " + this.agentA.ID + " b: " + this.agentB.ID);
		debug(debug, "\ta range " + constraints.domainPowerAgentA() + " b range: " + constraints.domainPowerAgentB());
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
		return -1;
	}
	
	public int getInternal(String key) {
		return 999;
	}

}



	
