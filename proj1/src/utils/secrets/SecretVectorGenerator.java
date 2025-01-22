package utils.secrets;

import java.util.Vector;

public class SecretVectorGenerator {

	private Vector<SecretGenerator> _generators;
	
	public SecretVectorGenerator(int[] secrets, int t) {
		_generators = new Vector<SecretGenerator>();
		// for each secret have a SecretGenerator
		for (int i = 0; i < secrets.length; i++) {
			SecretGenerator generator = new SecretGenerator(secrets[i], t);
			_generators.add(generator);
		}
	}
	
	public int[] GenerateVector() {
		int[] vector = new int[_generators.size()];
		int i = 0;
		for (SecretGenerator g: _generators) {
			vector[i] = g.GenerateValue();
			i++;
		}
		return vector;
	}

}
