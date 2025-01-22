package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class ClacLSBRespondMsg extends Message {
	
	public String seqKey;
	
	public ClacLSBRespondMsg(String seqKey) {
		this.seqKey = seqKey;
	} 
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	
}
