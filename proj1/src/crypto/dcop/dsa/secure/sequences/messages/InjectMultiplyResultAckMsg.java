package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class InjectMultiplyResultAckMsg extends Message {
	public String seqKey;
	
	public InjectMultiplyResultAckMsg(String seqKey) {
		this.seqKey = seqKey;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
