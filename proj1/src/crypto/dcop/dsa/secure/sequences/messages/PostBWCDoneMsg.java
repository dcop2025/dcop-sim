package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class PostBWCDoneMsg extends Message {

	public String lsbComputeSeqKey;
	public String bwcSeqKey;
	
	
	public PostBWCDoneMsg(String bwcSeqKey, String lsbComputeSeqKey) {
		this.lsbComputeSeqKey = lsbComputeSeqKey;
		this.bwcSeqKey = bwcSeqKey;
	}
	
	
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
