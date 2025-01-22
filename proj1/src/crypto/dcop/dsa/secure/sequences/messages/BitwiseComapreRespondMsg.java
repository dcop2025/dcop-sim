package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class BitwiseComapreRespondMsg extends Message {
	
	public String seqKey;
	public int phase;

	public BitwiseComapreRespondMsg(String seqKey, int phase) {
		this.seqKey = seqKey;
		this.phase = phase;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
