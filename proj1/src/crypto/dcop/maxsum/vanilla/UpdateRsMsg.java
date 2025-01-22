package crypto.dcop.maxsum.vanilla;

import java.math.BigInteger;

import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;
import java.util.Arrays;


public class UpdateRsMsg extends OMessage{
	private int otherID;
	private int round;
	private BigInteger[] r;
		
	
	public UpdateRsMsg(int otherID, int round, BigInteger[] r) {
		this.otherID = otherID;
		this.round = round;
		this.r = Arrays.copyOf(r, r.length);
	}
	
	public BigInteger[] r() {
		return r;
	}
	
	public String rString() {
        StringBuilder sb = new StringBuilder();
        for (BigInteger bigInt : r) {
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
	
	public int otherID() {
		return otherID;
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
