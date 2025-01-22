package crypto.dcop.dsa.secure.sequences.messages;

import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class MultiplySharesRespondMsg extends Message {
	public int ownerID;
	public String seqKey;
	public Shared c_2t_r;
	public String rKey;

	public MultiplySharesRespondMsg(int ownerID, String seqKey, Shared c_2t_r) {
		this.ownerID = ownerID;
		this.seqKey = seqKey;
		this.c_2t_r = c_2t_r;
		this.rKey = null;
	}
	
	@Override
	public Message clone() {
		return this;
	}
}
