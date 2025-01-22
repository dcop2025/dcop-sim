package crypto.dcop.maxsum.secure.messages;

import sinalgo.nodes.messages.Message;

public class MinIndexRespondMsg extends Message {
	public int index;	
	
	public MinIndexRespondMsg(int index) {
		this.index = index;		
	}
	
	@Override
	public String toString() {
		return String.format("index: %d", index);
	}

		
	@Override
	public Message clone() {
		return this;
	}


}
