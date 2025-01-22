package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class PostMultiLSBComputeMsg extends Message {

	public String lsbMultiSeqKey;
	public String bwcSeqKey;
	
	public PostMultiLSBComputeMsg(String lsbMultiSeqKey, String bwcSeqKey) {
		this.lsbMultiSeqKey = lsbMultiSeqKey;
		this.bwcSeqKey = bwcSeqKey;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
