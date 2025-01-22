package crypto.pdsa.messages;

import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;

/*
 *  SecureMultiplyRespondMsg s the message sent as a respond to secure compare
 *  This means the the multiplication operation is finish and the result is stored in the requested key 
 *  
 *  TODO: rename it as this trigger more then a simple multiplication request 
 */
public class SecureMultiplyRespondMsg extends OMessage {
	
	// The id of the key owner
	private int ownerID;
	// The index of the key to use (wb-<<ownerID>>-<<index>>)
	private int index;
	
    /**
     * Constructs a secure multiplication respond message
     *
     * @param the id of the key owner
     * @param the key to id which shared to reconstruct 
     */
	public SecureMultiplyRespondMsg(int owenrID, int index) {
		this.ownerID = owenrID;
		this.index = index;
	}
	
    /**
     * implements the clone pattern
     *
     * @return a clone of the message
     */
	@Override
	public Message clone() {
		return this;
	}
	
    /**
     * implements the action method, by calling the multiply respond message handler
     *
     * @param the agnet to handle the message
     * @param the node(agent) that sent the message
     */
	@Override
	public void action(Agent agent, Node sendner) {
		agent.HandleSecureMultiplyRespondMsg(sendner, this);
	}


	public int ownerID() {
		return this.ownerID;
	}
	
	public int index() {
		return this.index;
	}
	
}
