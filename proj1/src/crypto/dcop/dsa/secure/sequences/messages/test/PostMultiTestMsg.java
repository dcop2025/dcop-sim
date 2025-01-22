package crypto.dcop.dsa.secure.sequences.messages.test;

import sinalgo.nodes.messages.Message;

public class PostMultiTestMsg extends Message {
	public String ckey;
	
	public PostMultiTestMsg(String ckey) {
		this.ckey = ckey;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
