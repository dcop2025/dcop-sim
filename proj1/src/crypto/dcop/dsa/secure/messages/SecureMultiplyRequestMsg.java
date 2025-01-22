package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;


/*
 *  SecureMultiplyRequestMsg is the message sent to all agents 
 *  with request to do secure multiplication between two keys
 *  
 *  TODO: rename it as this trigger more then a simple multiplication request 
 */
public class SecureMultiplyRequestMsg extends Message{

	// The id of the key owner
	public int ownerID;
	// The index of the key to use (wb-<<ownerID>>-<<index>>) 
	public int targetIndex;
	
    /**
     * Constructs a secure multiplication request message
     *
     * @param the id of the key owner
     * @param the key to id which shared to reconstruct 
     */
	public SecureMultiplyRequestMsg(int ownerID, int targetIndex) {
		this.ownerID = ownerID;
		this.targetIndex = targetIndex;
	}
		
    /**
     * implements the clone pattern
     *
     * @return a clone of the message
     */
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}
}
