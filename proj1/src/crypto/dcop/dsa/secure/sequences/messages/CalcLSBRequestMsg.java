package crypto.dcop.dsa.secure.sequences.messages;

import sinalgo.nodes.messages.Message;

public class CalcLSBRequestMsg extends Message {
	
	public String seqKey;
	public String eKey;
	public String d0Key;
	public String ed0Key;
	public String resKey;
	
	public CalcLSBRequestMsg(String seqKey, String eKey, String d0Key, String ed0Key, String resKey) {
		this.seqKey = seqKey;
		this.eKey = eKey;
		this.d0Key = d0Key;
		this.ed0Key = ed0Key;
		this.resKey = resKey;
	} 
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	
}
