package crypto.dcop.maxsum.secure;

import crypto.dcop.*;
import crypto.dcop.Problem.ConstraintsMatrix;
import crypto.dcop.Network;
import crypto.dcop.maxsum.secure.brains.AgentBarin;
import crypto.dcop.maxsum.secure.brains.FunctionBarin;
import crypto.utils.Paillier;
import crypto.utils.PaillierMgr;
import sinalgo.runtime.Global;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;

public class NetworkGenerator {
	
	public static Network gererate(Problem problem, boolean fullyConnected) {
		Network network = new Network();
		
		
		BigInteger p = getGlobalPrime();
		Global.log.logln(true, "Selected P:" + p);
		
		Iterator<ConstraintsMatrix> constraintsIter = problem.iterator();
		Map<Integer, DcopAgent> agents = new HashMap<Integer, DcopAgent>();
		Map<Integer, AgentBarin> agentsBrains = new HashMap<Integer, AgentBarin>();
		PaillierMgr paillierMgr = new PaillierMgr(); 
				
		while (constraintsIter.hasNext()) {
			ConstraintsMatrix constraints = constraintsIter.next();
			if (constraints.zero()) {
				continue;
			}
			// Add constraints to agent A
			DcopAgent agentA = agents.get(constraints.a);
			if (agentA == null) {				
				agentA = new DcopAgent(constraints.a);
				AgentBarin brain = new AgentBarin(
						constraints.a, 
						agentA, 
						agentA.variabler(), 
						agentA.syncPoint(), 
						constraints.domainPowerAgentA(), 
						paillierMgr,
						p);
				agentA.setAlgo(brain);
				agents.put(constraints.a, agentA);
				network.agnets.add(agentA);
				agentsBrains.put(constraints.a, brain);
			}
			// Add constraints to agent B
			DcopAgent agentB = agents.get(constraints.b);
			if (agentB == null) {
				agentB = new DcopAgent(constraints.b);				
				AgentBarin brain = new AgentBarin(
						constraints.b,
						agentB, 
						agentB.variabler(), 
						agentB.syncPoint(), 
						constraints.domainPowerAgentB(), 
						paillierMgr,
						p);
				agentB.setAlgo(brain);
				agents.put(constraints.b, agentB);
				network.agnets.add(agentB);
				agentsBrains.put(constraints.b, brain);
			}
			agentA.addBidirectionalConnectionTo(agentB);
			
			DcopAgent functionNode = new DcopAgent(0);
			FunctionBarin brain = new FunctionBarin(
					functionNode, 
					functionNode.variabler(),
					agentA, agentB, 
					constraints, 
					paillierMgr, p);
			functionNode.setAlgo(brain);
			agentsBrains.get(constraints.a).addFunctionNode(constraints.b, functionNode);
			agentsBrains.get(constraints.b).addFunctionNode(constraints.a, functionNode);
			functionNode.addBidirectionalConnectionTo(agentA);
			functionNode.addBidirectionalConnectionTo(agentB);
			//network.functionNodes.add(functionNode);
			network.agnets.add(functionNode);
		}
				
		if (fullyConnected) {
			for (int i = 0; i < network.agnets.size() -1 ; i++) {
				for (int j = i + 1; j < network.agnets.size(); j++) {
					// no need connect agents that already agent
					if (network.agnets.get(i).outgoingConnections.contains(network.agnets.get(i), network.agnets.get(j))) {
						continue;
					}
					network.agnets.get(i).addBidirectionalConnectionTo(network.agnets.get(j));
				}
			}			
		}

		
		paillierMgr.logStatus();
		return network;
	}
	
	private static BigInteger getGlobalPrime() {
		
		Paillier paillier = new Paillier();
		return paillier.nsquare.subtract(BigInteger.ONE);
		
		//return BigInteger.valueOf(97);
	}
	
}
