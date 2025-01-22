package utils.secrets;

import java.util.Random;

public class SecretGenerator {
	static final boolean mock = true;
	
	private int[] _coefficients;
	private int _index;
	private int _secret;
	
	public SecretGenerator(int secret, int t) {
		_secret = secret;		
		_coefficients = generateRandomCoefficients(t);
		
	}
		
	private int[] generateRandomCoefficients(int numCoefficients) {
		Random rand = new Random();
		int[] coefficients = new int[numCoefficients];
        
        for (int i = 0; i < numCoefficients; i++) {
        	int coefficient = rand.nextInt(100);
        	
            coefficients[i] = coefficient;
        }

        return coefficients;
    }

	
	public int GenerateValue() {
		if (mock) {
			return _secret;
		}
		// Set a new index
		_index++;
		// Send the secret as the free coefficient
		int value = _secret;
		int x = 1;		
		for (int i = 0; i < _coefficients.length; i++) {
			// Raise the x power
			x = x * _index;
			// Calc the current coefficient
			value = value + _coefficients[i] * _index;
		}
		return value;

	}
}
