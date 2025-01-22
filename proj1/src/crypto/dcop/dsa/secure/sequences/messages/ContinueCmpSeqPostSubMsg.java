package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class ContinueCmpSeqPostSubMsg extends Message {
	public String subSeqKey;
	public String cmpSeqKey;

	public ContinueCmpSeqPostSubMsg(String cmpSqeKey, String subSeqKey) {
		this.subSeqKey = subSeqKey;
		this.cmpSeqKey = cmpSqeKey;
	}
	
	@Override
	public Message clone() {
		return this;
	}
}
