package crypto.dcop.dsa.secure.sequences.messages.test;

import sinalgo.nodes.messages.Message;

public class InjectTestDoneMsg extends Message {

	public String seqKey;
	public String testType;
	
	public InjectTestDoneMsg(String seqKey, String testType) {
		this.seqKey = seqKey;
		this.testType = testType;
	}
		
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
