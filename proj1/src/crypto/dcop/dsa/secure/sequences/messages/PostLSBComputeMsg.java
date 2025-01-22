package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class PostLSBComputeMsg extends Message {

	public String lsbCmpSeqKey;
	public String cmpSeqKey;
	
	public PostLSBComputeMsg(String lsbCmpSeqKey, String cmpSeqKey) {
		this.lsbCmpSeqKey = lsbCmpSeqKey;
		this.cmpSeqKey = cmpSeqKey;
	}
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return null;
	}

}
