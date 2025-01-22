package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;
import java.util.Arrays;
import crypto.utils.shamir.Shared;

public class ConstrainsRowSharingMsg extends Message{
	public int agentA;
	public int agentB;
	public int round;
	public Shared[] shareds;
	
	public ConstrainsRowSharingMsg(int aID, int bID, int round, Shared[] shareds) {
		agentA = aID;
		agentB = bID;
		this.round = round;
		this.shareds = Arrays.copyOf(shareds, shareds.length);
	}
	
	@Override
	public Message clone() {
		return this;
	}

}