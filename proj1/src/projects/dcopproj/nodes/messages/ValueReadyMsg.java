package projects.dcopproj.nodes.messages;

import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;

public class ValueReadyMsg extends OMessage {
	
	@Override
	public Message clone() {
		return new ValueReadyMsg();
	}
	

	@Override
	public void action(Agent agent, Node sendner) {
		agent.HandleValueReadyMsg(sendner, this);
	}
}
