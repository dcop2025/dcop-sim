package crypto.dcop.dsa.secure.sequences.messages.test;

import sinalgo.nodes.messages.Message;

public class PostCompareTestMsg  extends Message {
	public String seqKey;
	
	public PostCompareTestMsg(String seqKey) {
		this.seqKey = seqKey;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
