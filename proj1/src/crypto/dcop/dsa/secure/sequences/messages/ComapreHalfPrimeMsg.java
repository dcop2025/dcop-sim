package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class ComapreHalfPrimeMsg extends Message {

	public String seqKey;
	public String xKey;
	
	public ComapreHalfPrimeMsg(String seqKey, String xKey) {
		this.seqKey = seqKey;
		this.xKey = xKey;
	}
	
	public String x2Key() {
		return this.xKey+"x2";
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	
}
