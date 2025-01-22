package utils.dcopgen;

import java.util.Random;

public class AgentAttributes {
	private int _rangeLow;
	private int _rangeHigh;
	private Random _random = new Random();
	
	public AgentAttributes(int rangeLow, int rangeHigh) {
		_rangeLow = rangeLow;
		_rangeHigh = rangeHigh;
	}
	
	public int DomainRange() {
		return _random.nextInt(_rangeHigh - _rangeLow) + _rangeLow;
	}
		
}