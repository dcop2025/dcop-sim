package crypto.pdsa.messages;

import common.framework.messages.OMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import utils.dcopgen.Agent;

/*
 *  SecureCompareRequestMsg is the message sent to all agents 
 *  with request to do secure compare between two keys
 */
public class SecureCompareRequestMsg extends OMessage{
   
	// left operand 
	private String left;
	// right operand	
	private String right;
	//  result operand
	private String result;
	// The id of the keys owner
	private int ownerID;
	// an opaque to return with the respond
	private int opaque; 
	
    /**
     * Constructs a secure compare request message
     *
     * @param the key of left operand
     * @param the key of right operand
     * @param the key of compare result
     * @param the id of the key owner
     * @param opaque to return with the respond 
     */
	public SecureCompareRequestMsg(
			String leftKey, 
			String rightKey, 
			String resultKey, 
			int ownerID, 
			int opaque) {
		this.left = leftKey;
		this.right = rightKey;
		this.result = resultKey;
		this.ownerID = ownerID;
		this.opaque = opaque;
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
     * implements the action method, by calling the secure compare request message handler
     *
     * @param the agnet to handle the message
     * @param the node(agent) that sent the message
     */
	@Override
	public void action(Agent agent, Node sendner) {
		agent.HandleSecureCompareRequestMsg(sendner, this);
	}
	
	public String Left() {
		return left;
	}
	public String Right() {
		return right;
	}
	public String Result() {
		return result;
	}

	public int ownerID() {
		return ownerID;
	}
	
	public int opaque() {
		return opaque;
	}
}