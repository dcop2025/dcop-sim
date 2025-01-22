package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;

public class RequestSharedMsg extends Message {

	public String key;
	
	public RequestSharedMsg(String key) {
		this.key = key;
	}
	
	@Override
	public Message clone() {
		return this;
	}
	
}
