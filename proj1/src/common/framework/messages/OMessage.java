package common.framework.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;

// OMessage is a an interface class that extends the sinalgo message
// and provide action method, to enable dependency inversion pattern
public abstract class OMessage extends Message{ 	

    /**
     * calls a predefine action method in the Agent to handle the message
     * must be implement by every message, by calling the proper Agent handle message method 
     *
     * @param the Agent to handle the message, should be the one that calls the action method
     * @param the node that send the message 
     */
	public abstract void action(Agent agent, Node sendner);
}
