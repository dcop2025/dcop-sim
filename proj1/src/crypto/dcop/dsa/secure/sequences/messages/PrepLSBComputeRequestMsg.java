package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class PrepLSBComputeRequestMsg extends Message {
	public String inputKey;
	public String rKey;
	public String seqKey;
	
	public PrepLSBComputeRequestMsg(String inputKey, String rKey, String seqKey) {
		this.inputKey = inputKey;
		this.rKey = rKey;
		this.seqKey = seqKey;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

	

}
