/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.dcopproj;


import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import crypto.dcop.DcopAgent;
import crypto.dcop.Problem;
import crypto.dcop.ProblemGenerator;
import crypto.dcop.ScaleFreeGenerator;
import crypto.dcop.Network;
import crypto.dcop.maxsum.vanilla.NetworkGenerator;
import crypto.utils.OModMath;
import crypto.utils.Paillier;
import crypto.utils.shamir.Generater;
import crypto.utils.shamir.PowersStash;
import crypto.utils.shamir.ShamirSharedGen;
import crypto.utils.shamir.Shared;
import projects.defaultProject.models.distributionModels.Random;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import utils.dcopgen.Agent;
import utils.dcopgen.DcopGenerator;
import utils.dcopgen.AgentAttributes;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	 enum AlgoType {
		 	NONE,
		 	DSA,
		    PDSA,
		    PMAXSUM,
		    MAXSUM,
		  }
	 
	Network network;
	Problem problem; 
	AlgoType algoType;
	long pgSeed = 1001L;
	long networkSeed = 2001L;
	int agent = 5;
	double nd = 0.8;
	int range = 5;
	int minCost = 0;
	int maxCost = 10;
	int round = 0;
	int lastRound = 20;	

	
	class TestConfiguration {
		int agents;
		int domainSize;
		double nd;
		double durtion;
		int totalCost;

		public TestConfiguration(int agents, int domain, double nd, double durtion) {
			this.agents = agents;
			this.domainSize = domain;
			this.nd = nd;
			this.durtion = durtion;
			this.totalCost = 0;
		}
	}
	
	private TestConfiguration[] genTestConfiguration() {
		TestConfiguration[] configs = new TestConfiguration[1];
				
		configs[0] = new TestConfiguration(10, 10 , 0.4, 60000);
		return configs;
	}

	
	ProblemGenerator pg = new ProblemGenerator(agent, nd, range, range, minCost, maxCost, pgSeed);
	//ProblemGenerator pg;
	ScaleFreeGenerator scaleFreeGenerator;
	long startTime;
	
	TestConfiguration[] tests = genTestConfiguration();
	int testIndex = 0;

	
	private boolean areAgentsDoneStatus() {
		for (DcopAgent agent : network.agnets) {
			if (!agent.doneStatus()) {
				return false;
			}			
		}
		return true;
	}
	
	private boolean isTestTimeOut() {
		long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // Time in milliseconds

        return (tests[testIndex].durtion < duration);
	}
	
	private boolean isTestDone() {
		return (areAgentsDoneStatus() || isTestTimeOut());
	}
	
	private boolean iterNextTest() {
		// every test configuration runs for 50 times (lastRound)

		Global.log.logln(LogL.ALWAYS, String.format("Round: %d", round));


		if (round == lastRound) {
			
			// after the last round, go to the next test configuration
			// but before that, log the cost
			
			Global.log.logln(LogL.ALWAYS, String.format("Result: agnets %d domain size %d nd %.2f durtion %.2f, cost %d",
					tests[testIndex].agents, tests[testIndex].domainSize, tests[testIndex].nd, tests[testIndex].durtion, tests[testIndex].totalCost/lastRound));
			testIndex++;
			if (testIndex == tests.length) {
				return false;
			}
			round = 0;
		}
		// always in seeds
		pgSeed++;
		networkSeed++;

		kickStartAlgo();
		return true;
	}

	private void kickStartAlgo() {
		if (algoType == AlgoType.PDSA) {
			kickStartPDSATesting();
			return;
		}
		if (algoType == AlgoType.PMAXSUM) {
			kickStartPMaxSum();
			return;
		}
		
		if (algoType == AlgoType.DSA) {
			kickStartDSATesting();
		}
		
		if (algoType == AlgoType.MAXSUM) {
			kickStartMaxSum();
		}
	}		 
	

	
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		if (network == null) {
			return true;
		}
		
		
		if (!isTestDone()) {
			return false;
		}
		
		
		
		round++;
		// relog for sense of progression
		long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // Time in milliseconds
        System.out.println("Execution time: " + duration + " milliseconds");
        
        accDuration = accDuration + duration;
		calcCost();
		
		cleanNetwork();
		boolean next = iterNextTest();
		return !next;
		
	}
	
	int acclMcpComapre = 0;
	int accMcpMult = 0;
	int accCost = 0;
	int accRounds = 0;
	long accDuration = 0L;
	
	private void calcCost() {
		Map<Integer, Integer> assigments = new HashMap<Integer, Integer>();
		for (DcopAgent agent : network.agnets) {
			if (agent.assignment() == -1) {
				continue;
			}
			
			Global.log.logln(LogL.ALWAYS, String.format("ID: %d assignment %s", agent.agentId, agent.assignment()));
			assigments.put(agent.agentId, agent.assignment());
			int around = agent.getInternal("round");
			if (around == 1) {
				Global.log.logln(LogL.ALWAYS, String.format("didn't finish running"));	
			}
			if (agent.agentId == 1) {
				accRounds += (around - 1); 
				Global.log.logln(LogL.ALWAYS, String.format("agent 1: round %d", around));
			}
			
		}
		int cost = problem.solve(assigments);
		accCost = accCost + cost;
		tests[testIndex].totalCost = tests[testIndex].totalCost + cost; 
		Global.log.logln(LogL.ALWAYS, String.format("Total cost: %d", cost));

		if (algoType == AlgoType.PDSA) {
			int totalMcpComapre = 0;
			int totalMcpMult = 0;
			for (DcopAgent agent : network.agnets) {
				totalMcpComapre = totalMcpComapre + agent.getInternal("mcp_comapre");
				totalMcpMult = totalMcpMult + agent.getInternal("mcp_mult");
			}
			acclMcpComapre = acclMcpComapre + totalMcpComapre;
			accMcpMult = accMcpMult + totalMcpMult;
			//Global.log.logln(LogL.ALWAYS, String.format("Total comapre: %d", totalMcpComapre));
			//Global.log.logln(LogL.ALWAYS, String.format("Total multi: %d", totalMcpMult));
			
		}
		
		double avgDuration = accDuration/round;
		double avgMcpComapre = acclMcpComapre/round;
		double avgMcpMult = accMcpMult/round;
		double avgCost = (double)accCost/(double)round;
		double avgRounds = accRounds/round;
		Global.log.logln(LogL.ALWAYS, String.format("Avg cost: %.2f", avgCost));
		Global.log.logln(LogL.ALWAYS, String.format("Avg accDuration: %.2f", avgDuration));
		Global.log.logln(LogL.ALWAYS, String.format("Avg round: %.2f", avgRounds));
		//Global.log.logln(LogL.ALWAYS, String.format("Avg comapre: %.2f", avgMcpComapre));
		//Global.log.logln(LogL.ALWAYS, String.format("Avg multi: %.2f", avgMcpMult));			
		
	}
	
	@AbstractCustomGlobal.CustomButton(buttonText="Trigger Max Sum full flow", toolTipText="Trigger Max sum")
	public void TriggerMaxSum() {
		Global.log.logln(LogL.ALWAYS, "Trigger MaxSum");

		algoType = AlgoType.MAXSUM;
		kickStartMaxSum();

	}


	@AbstractCustomGlobal.CustomButton(buttonText="Trigger Private Max Sum full flow", toolTipText="Trigger Max sum")
	public void TriggerPrivateMaxSum() {
		Global.log.logln(LogL.ALWAYS, "Trigger Private MaxSum");

		algoType = AlgoType.PMAXSUM;
		kickStartPMaxSum();
		
	}


	
	@AbstractCustomGlobal.CustomButton(buttonText="Trigger Private DSA full flow", toolTipText="Trigger Private ")
	public void TriggerPDSAFlow() {
		
		algoType = AlgoType.PDSA;
		Global.log.logln(LogL.ALWAYS, "Trigger PDSA, round :" + round);

		// To ensure stash is created
		PowersStash.getInstance();
		
		kickStartPDSATesting();
		
	}
	
	private void kickStartPDSATesting() {
		if (testIndex >= this.tests.length) {
			Global.log.logln(LogL.ALWAYS, "no more testing");
		}
		TestConfiguration config = tests[testIndex];
		ProblemGenerator pg = new ProblemGenerator(config.agents, config.nd, config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		//ScaleFreeGenerator pg = new ScaleFreeGenerator(config.agents, 4, 2,config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		
		problem = pg.Generate();
			
		deployNetwork(crypto.dcop.dsa.secure.NetworkGenerator.gererate(problem, 100, 0.7, networkSeed));
	}
	
	private void kickStartDSATesting() {
		if (testIndex >= this.tests.length) {
			Global.log.logln(LogL.ALWAYS, "no more testing");
		}
		TestConfiguration config = tests[testIndex];
		ProblemGenerator pg = new ProblemGenerator(config.agents, config.nd, config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		//ScaleFreeGenerator pg = new ScaleFreeGenerator(config.agents, 4, 2,config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		
		
		problem = pg.Generate();
		deployNetwork(crypto.dcop.dsa.vanilla.NetworkGenerator.gererate(problem, false, networkSeed));		
	}
	
	private void kickStartPMaxSum() {
		if (testIndex >= this.tests.length) {
			Global.log.logln(LogL.ALWAYS, "no more testing");
		}
		TestConfiguration config = tests[testIndex];
		ProblemGenerator pg = new ProblemGenerator(config.agents, config.nd, config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		//ScaleFreeGenerator pg = new ScaleFreeGenerator(config.agents, 4, 2,config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		
		problem = pg.Generate();
			
		deployNetwork(crypto.dcop.maxsum.secure.NetworkGenerator.gererate(problem, false));
		
	}
	
	private void kickStartMaxSum() {
		if (testIndex >= this.tests.length) {
			Global.log.logln(LogL.ALWAYS, "no more testing");
		}
		TestConfiguration config = tests[testIndex];
		ProblemGenerator pg = new ProblemGenerator(config.agents, config.nd, config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		//ScaleFreeGenerator pg = new ScaleFreeGenerator(config.agents, 4, 2,config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		
		problem = pg.Generate();
			
		deployNetwork(crypto.dcop.maxsum.vanilla.NetworkGenerator.gererate(problem, false));
		

	}
	
	@AbstractCustomGlobal.CustomButton(buttonText="Trigger DSA full flow", toolTipText="Trigger DSA")
	public void TriggerDSAFlow() {
		Global.log.logln(LogL.ALWAYS, "Trigger DSA");
		
		TestConfiguration config = tests[testIndex];
		ProblemGenerator pg = new ProblemGenerator(config.agents, config.nd, config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		//ScaleFreeGenerator pg = new ScaleFreeGenerator(config.agents, 4, 2,config.domainSize, config.domainSize, minCost, maxCost, pgSeed);
		problem = pg.Generate();
		
		deployNetwork(crypto.dcop.dsa.vanilla.NetworkGenerator.gererate(problem, false, 2323L));

	}

	
	
	
	private void deployNetwork(Network network) {
		this.network = network;

		Random random = new Random();
		for (DcopAgent agent: network.agnets) {
			agent.setPosition(random.getNextPosition());
			agent.finishInitializationWithDefaultModels(true);
			Runtime.addNode(agent);			
		}
		Tools.repaintGUI();

		for (DcopAgent agent: network.agnets) {
			agent.logState();
		}
		
		startTime = System.currentTimeMillis();
		for (DcopAgent agent: network.agnets) {
			agent.triggerAlgo();
		}
	}

	
	private void cleanNetwork() {

		network =  null;
		problem = null;
		//algoType = AlgoType.NONE;

		Tools.removeAllNodes();
		//pg = new ProblemGenerator(agent, nd, range, range, minCost, maxCost, pgSeed);
	}
	
	public void preRound() {
		// Stub
	}
	
}
 