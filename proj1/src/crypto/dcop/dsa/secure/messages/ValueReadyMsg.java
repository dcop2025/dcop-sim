package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;

public class ValueReadyMsg extends Message {
	
	@Override
	public Message clone() {
		return new ValueReadyMsg();
	}
	
}
