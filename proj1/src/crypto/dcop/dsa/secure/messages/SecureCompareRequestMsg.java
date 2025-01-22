package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;

/*
 *  SecureCompareRequestMsg is the message sent to all agents 
 *  with request to do secure compare between two keys
 */
public class SecureCompareRequestMsg extends Message{
   
	// left operand 
	public String left;
	// right operand	
	public String right;
	//  result operand
	public String result;
	// The id of the keys owner
	public int ownerID;
	// an opaque to return with the respond
	public int opaque; 
	
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
		left = leftKey;
		right = rightKey;
		result = resultKey;
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
}