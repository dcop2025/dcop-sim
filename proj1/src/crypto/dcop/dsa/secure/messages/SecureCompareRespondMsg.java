package crypto.dcop.dsa.secure.messages;

import sinalgo.nodes.messages.Message;

/*
 *  SecureCompareRespondMsg is the message sent as a respond to secure compare
 *  This means the the compare operation is finish and the result is stored in the requested key
 */
public class SecureCompareRespondMsg extends Message{

	// the given opaque to return with the respond
	private int opaque;
	
    /**
     * Constructs a secure compare respond message
     *
     * @param opaque to return with the respond 
     */
	public SecureCompareRespondMsg(int opaque) {
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

	public int opaque() {
		return this.opaque;
	}
	
}