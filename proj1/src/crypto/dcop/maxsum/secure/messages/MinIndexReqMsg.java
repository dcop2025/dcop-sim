package crypto.dcop.maxsum.secure.messages;

import java.math.BigInteger;
import java.util.Arrays;

import sinalgo.nodes.messages.Message;

public class MinIndexReqMsg extends Message {
	public int source;	
	public BigInteger[] m;
	
	public MinIndexReqMsg(int source, BigInteger[] m) {
		this.source = source;
		this.m = Arrays.copyOf(m, m.length);		
	}
	
	@Override
	public String toString() {
        StringBuilder sb = new StringBuilder();
       	sb.append(String.format("source: %d m: ", source));
        for (BigInteger bigInt : m) {
            sb.append(bigInt.toString()).append(", ");
        }
        
        // Remove the last comma and space if needed
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
	}

		
	@Override
	public Message clone() {
		return this;
	}

}
