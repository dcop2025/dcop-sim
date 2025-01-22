package utils.dcopgen;

import java.util.Vector;

import sinalgo.runtime.Global;
import sinalgo.tools.logging.LogL;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;


public class DcopGenerator {
	
	private Random _random;
		
	public Vector<Agent> GeneratorProblem(Vector<AgentAttributes> agentsAttributes, float networkDensity) {
		_random = new Random();		
		Vector<Agent> _agents = new Vector<Agent>();
		

		// Create Agents
		for (AgentAttributes attr: agentsAttributes) {
			Agent agnet = new Agent(attr.DomainRange());
			agnet.finishInitializationWithDefaultModels(true);
			_agents.add(agnet);			
		}
		// Create a connection + cost matrix
		int numOfAgnets = agentsAttributes.size();
		for (int i = 0; i < numOfAgnets -1 ; i++) {
			for (int j = i + 1; j < numOfAgnets; j++) {
				GroomAgents(_agents.get(i), _agents.get(j), networkDensity);
			}
		}
		
		return _agents;
	}
	
	public void FullConnectNetwork(Vector<Agent> agents) {
		int numOfAgnets = agents.size();
		for (int i = 0; i < numOfAgnets -1 ; i++) {
			for (int j = i + 1; j < numOfAgnets; j++) {
				// no need connect agents that already anget
				if (agents.get(i).outgoingConnections.contains(agents.get(i), agents.get(j))) {
					continue;
				}
				agents.get(i).addBidirectionalConnectionTo(agents.get(j));
			}
		}
	}
	
	private void GroomAgents(Agent a, Agent b, float networkDensity) {
		boolean connected = (networkDensity >= _random.nextFloat());
		int factor;
		if (connected) {
			factor = 1;
		} else {
			factor = 0;
		}
		int n = a.DomainRange();
		int m = b.DomainRange();
		int[][] matrixA = new int[n][m];	        
		int[][] matrixB = new int[m][n];		 
	    for (int i = 0; i < n; i++) {
	    	for (int j = 0; j < m; j++) {
	    		matrixA[i][j] = _random.nextInt(11) * factor;
	    		matrixB[j][i] = matrixA[i][j];	    		 
	    	}
	    }
	    a.AddConnectionConstraints(b, matrixA);
	    b.AddConnectionConstraints(a, matrixB);
	}
	
	public int CalcCost(Vector<Agent> agents) {
		int cost = 0;
		Global.log.logln(LogL.ALWAYS, ">> Calc Cost");
		for (Agent agentA : agents) {
			Global.log.logln(LogL.ALWAYS, ">> ID: " + agentA.ID + " index: " + agentA.Index());
			for (Agent agentB: agents) {
				if (agentA.ID >= agentB.ID) {
					continue;
				}
				int pairCost = agentA.GetConstraint(agentB.ID, agentA.Index(), agentB.Index());
				Global.log.logln(LogL.ALWAYS, "   >> Other ID: " + agentB.ID + " index: " + agentB.Index() + " pair cost: " + pairCost);
				cost += pairCost;
			}
		}
		return cost;
	}

}
