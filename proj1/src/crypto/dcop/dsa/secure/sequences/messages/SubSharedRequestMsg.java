package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class SubSharedRequestMsg extends Message {
	public String aKey;
	public String bKey;
	public String resKey;
	public String subSeqKey; 
	

	public SubSharedRequestMsg(String aKey, String bKey, String resKey, String subSeqKey) {
		this.aKey = aKey;
		this.bKey = bKey;
		this.resKey = resKey;
		this.subSeqKey = subSeqKey;		
	}



	@Override
	public Message clone() {
		return this;
	}
	
}
