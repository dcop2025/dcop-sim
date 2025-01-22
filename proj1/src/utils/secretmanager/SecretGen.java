package utils.secretmanager;

import java.util.ArrayList;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import utils.secretmanager.SharedSecret;


// SecretGen is a utilize that generates (t,n) secrets
public class SecretGen {
	
	public static List<SharedSecret> GenNewSecret(String key, int secret, int thershold, int peers) {
		
		List<SharedSecret> shareds = new ArrayList<SharedSecret>();
		
		// Create shared secret
		List<BigInteger> coefficients = generateRandomCoefficients(peers-1);
		for (int i = 0; i < peers; i++) {
			BigInteger value = calcCoefficients(coefficients, secret, i + 1);
			SharedSecret shared = new SharedSecret(i+1, value);
			shareds.add(shared);
		}
		
		return shareds;
	}
	
	private static List<BigInteger> generateRandomCoefficients(int numCoefficients) {
		Random rand = new Random();
		List<BigInteger> coefficients = new ArrayList<BigInteger>();
        
        for (int i = 0; i < numCoefficients; i++) {
        	BigInteger coefficient = BigInteger.valueOf(rand.nextInt(100));
            coefficients.add(coefficient);
        }

        return coefficients;
    }
 	
	private static BigInteger calcCoefficients(List<BigInteger> coefficients, int secret, int index) {
		BigInteger value = BigInteger.valueOf(secret);
		BigInteger x = BigInteger.valueOf(1);
		for (int i = 0; i < coefficients.size(); i++) {
			x = x.multiply(BigInteger.valueOf(index));
			value = value.add(coefficients.get(i).multiply(x));
		}
		return value;
	}
	
}
