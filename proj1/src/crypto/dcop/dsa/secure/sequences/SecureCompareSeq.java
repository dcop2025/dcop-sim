package crypto.dcop.dsa.secure.sequences;


import sinalgo.nodes.messages.Message;

public class SecureCompareSeq implements ISequence {
	public Message postMsg;
	public String aKey;
	public String bKey;
	public String resKey;
			
	public SecureCompareSeq(String aKey, String bKey, String resKey,  Message postMsg) {
		this.postMsg = postMsg;	
		this.aKey = aKey;
		this.bKey = bKey;
		this.resKey = resKey;
	}
		
}
