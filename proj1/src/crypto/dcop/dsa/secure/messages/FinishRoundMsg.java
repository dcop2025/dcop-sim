package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;

public class FinishRoundMsg  extends Message{
	public int ID;
	public int round;
	
	public FinishRoundMsg(int ID, int round) {
		this.ID = ID;
		this.round = round;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}