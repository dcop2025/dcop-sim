package crypto.dcop.maxsum.secure.messages;

import java.math.BigInteger;
import java.util.Arrays;

import sinalgo.nodes.messages.Message;

public class ProcessWsRequestMsg extends Message {
	public int round;
	public int source;
	public int target;
	public BigInteger[][] Ws;
	public BigInteger opaque;
	
	public ProcessWsRequestMsg(int round, int source, int target, BigInteger[][] Ws, BigInteger opaque) {
		this.round = round;
		this.source = source; 
		this.target = target;
		this.Ws = new BigInteger[Ws.length][Ws[0].length];
        for (int i = 0; i < Ws.length; i++) {
        	this.Ws[i] = Arrays.copyOf(Ws[i], Ws[i].length);
        }
        this.opaque = opaque;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
       	sb.append(String.format("round: %d source: %d target %d opaque: %s", round, source, target, opaque));
		
		
		for (int i = 0; i < Ws.length; i++) {
			String line = "\t[";
			for (int j = 0; j < Ws[i].length; j++) {
				line = line + " " + Ws[i][j];
			}
			line = line + " ]";
			sb.append(line);
		}
		
        return sb.toString();
	}
	
	@Override
	public Message clone() {
		return this;
	}
}
