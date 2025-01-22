package crypto.dcop.dsa.secure.sequences.messages;

import crypto.utils.shamir.Shared;
import sinalgo.nodes.messages.Message;

public class BitwiseCompareReady extends Message {

	public String seqKey;
	public Shared c;
	
	public BitwiseCompareReady(String seqKey, Shared c) {
		this.seqKey = seqKey;
		this.c = c;
	}
		
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	
}
