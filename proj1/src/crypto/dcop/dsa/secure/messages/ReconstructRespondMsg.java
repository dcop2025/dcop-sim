package crypto.dcop.dsa.secure.messages;

import java.math.BigInteger;

import sinalgo.nodes.messages.Message;

/*
 *  ReconstructRespondMsg is the reply on ReconstructRequestMsg,
 *  it contains the key, and a value that will be used to reconstruct  
 */
public class ReconstructRespondMsg extends Message {

	// The id of the key owner
	private int owerID;
	// The key to reconsturct
	private String key;
	// The value of the key
	private BigInteger value;
	
	
    /**
     * Constructs a reconstruct respond message
     *
     * @param the id of the key owner
     * @param the key to id which shared to reconstruct
     * @param the value of the key
     */
	public ReconstructRespondMsg(int id, String key, BigInteger value) {
		this.owerID = id;
		this.key = key;
		this.value = value;
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

	public int ownerID() {
		return this.owerID;
	}

	public String key() {
		return this.key;
	}
	
	public BigInteger value() {
		return this.value;
	}

}