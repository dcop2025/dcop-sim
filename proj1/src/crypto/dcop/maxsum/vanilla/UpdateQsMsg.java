package crypto.dcop.maxsum.vanilla;

import java.math.BigInteger;

import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;
import java.util.Arrays;


public class UpdateQsMsg extends OMessage{
	private int targetId;
	private int round;
	private BigInteger[] q;
	
	
	public UpdateQsMsg(int targetId, int round, BigInteger[] q) {
		this.targetId = targetId;
		this.round = round;
		this.q = Arrays.copyOf(q, q.length);
	}
	
	public int targetId() {
		return targetId;
	}
	
	public BigInteger[] q() {
		return q;
	}
	
	public String qString() {
        StringBuilder sb = new StringBuilder();
        for (BigInteger bigInt : q) {
            sb.append(bigInt.toString()).append(", ");
        }
        
        // Remove the last comma and space if needed
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
	}
	
	public int round() {
		return round;
	}

	@Override
	public Message clone() {
		return this;
	}

	// TODO delete this
	@Override
	public void action(Agent agent, Node sendner) {
		// TODO Auto-generated method stub
		
	}
}
