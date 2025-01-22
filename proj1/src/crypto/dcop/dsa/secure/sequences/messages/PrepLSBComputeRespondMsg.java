package crypto.dcop.dsa.secure.sequences.messages;

import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class PrepLSBComputeRespondMsg extends Message {

	public String seqKey;
	public Shared shared;
	public PrepLSBComputeRespondMsg(Shared shared, String seqKey) {
		this.shared = shared;
		this.seqKey = seqKey;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	
}
