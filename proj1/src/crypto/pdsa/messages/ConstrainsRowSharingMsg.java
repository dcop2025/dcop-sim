package crypto.pdsa.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;
import java.util.Arrays;

import common.framework.messages.OMessage;


/* 
 * ConstrainsRowSharingMsg is a message that contains a constrain row as shared secret
 * from an Anget a about Agent B
 */
public class ConstrainsRowSharingMsg  extends OMessage{
	// The ID of agent A, the agent that holds the constrains
	private int agentA;
	// The ID of agent B, the agent the constrains applied to
	private int agentB;
	// The PDSA round
	private int round;
	// the shares of the constrains
	private int[] shares;
	
	public ConstrainsRowSharingMsg(int aID, int bID, int round, int[] shares) {
		this.agentA = aID;
		this.agentB = bID;
		this.round = round;
		this.shares = Arrays.copyOf(shares, shares.length);
	}
	
	@Override
	public Message clone() {
		return this;
	}

	@Override
	public void action(Agent agent, Node sendner) {
		agent.HandleShareSecretVectorMsg(sendner, this);
	}
	
	public int a() {
		return this.agentA;
	}

	public int b() {
		return this.agentB;
	}

	public int round() {
		return round;
	}

	public int[] shares() {
		return this.shares;
	}
	
}
