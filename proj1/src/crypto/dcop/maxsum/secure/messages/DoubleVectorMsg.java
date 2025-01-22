package crypto.dcop.maxsum.secure.messages;

import java.math.BigInteger;
import java.util.Arrays;

import sinalgo.nodes.messages.Message;
 
public class DoubleVectorMsg extends Message {
	public int round;
	public int source;	
	public int target;
	public BigInteger[] local;
	public BigInteger[] remote;
	
	public DoubleVectorMsg(int round,int source, int target, BigInteger[] local, BigInteger[] remote) {
		this.round = round;
		this.source = source;
		this.target = target;
		this.local = Arrays.copyOf(local, local.length);
		if (remote != null) {
			this.remote = Arrays.copyOf(remote, remote.length);	
		} 				
	}
	
	public String toString(boolean full) {
        StringBuilder sb = new StringBuilder();
        if (full) {
        	sb.append(String.format("round: %d source: %d target: %d v1: [", round, source, target));
        }
        for (BigInteger bigInt : local) {
            sb.append(bigInt.toString()).append(", ");
        }
        
        // Remove the last comma and space if needed
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        
        
        sb.append("] v2: [");
        if (remote != null) {
        	for (BigInteger bigInt : remote) {
        		sb.append(bigInt.toString()).append(", ");
        	}
        } else {
        	sb.append("null  ");
        }
        
        // Remove the last comma and space if needed
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
	}

		
	@Override
	public Message clone() {
		return this;
	}
}
