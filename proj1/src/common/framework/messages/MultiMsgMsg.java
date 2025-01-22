package common.framework.messages;

import java.util.Vector;

import sinalgo.nodes.messages.Message;

public class MultiMsgMsg extends Message {
	public Vector<Message> messages;
	
	public MultiMsgMsg() {
		messages = new Vector<Message>();
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
