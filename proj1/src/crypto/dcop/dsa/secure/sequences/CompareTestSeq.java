package crypto.dcop.dsa.secure.sequences;


import sinalgo.nodes.messages.Message;

public class CompareTestSeq implements ISequence {
	public Message postMsg;
	public String aKey;
	public String bKey;
	public String cKey;
	public int injected;
			
	public CompareTestSeq(String aKey, String bKey, String cKey,  Message postMsg) {
		this.postMsg = postMsg;	
		this.aKey = aKey;
		this.bKey = bKey;
		this.cKey = cKey;
		this.injected = 0;
	}
		
}
