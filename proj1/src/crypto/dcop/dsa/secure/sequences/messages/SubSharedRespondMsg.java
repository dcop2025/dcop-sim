package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class SubSharedRespondMsg extends Message {
	public String seqKey;

	public SubSharedRespondMsg(String seqKey) {
		this.seqKey = seqKey;
	}
	
	@Override
	public Message clone() {
		return this;
	}
}