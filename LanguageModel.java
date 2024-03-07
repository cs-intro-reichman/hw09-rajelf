import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        String fileString = "";
        In input = new In(fileName);
        fileString = input.readAll();
        for (int i = 0; i + windowLength < fileString.length(); i++) {
            String key = fileString.substring(i, i + windowLength);
            List value = CharDataMap.get(key);
            if (value != null) {
                if (value.indexOf(fileString.charAt(i + windowLength)) != -1) {
                    value.update(fileString.charAt(i + windowLength));

                } else {
                    value.addFirst(fileString.charAt(i + windowLength));
                }
            } else {
                CharDataMap.put(key, new List());
                CharDataMap.get(key).addFirst(fileString.charAt(i + windowLength));
            }
            calculateProbabilities(CharDataMap.get(key));
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		    // First, calculate the total number of characters
            int totalChars = 0;
            for (CharData cd : probs.toArray()) {
                totalChars += cd.count;
            }
        
            // Now calculate and set the probabilities (p and cp)
            double acomulativeProbability = 0.0;
            for (CharData cd : probs.toArray()) {
                cd.p = (double) cd.count / totalChars; // Calculate the probability of each character
                acomulativeProbability += cd.p; // Update 
                cd.cp = acomulativeProbability; // Set the cumulative probability for the character
            }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        double r = randomGenerator.nextDouble(); // random number in [0,1)
        CharData[] charDataArray = probs.toArray(); // Assuming List has a toArray() method returning CharData[]
        
        // Iterate through the list until finding the character whose cumulative probability is greater than r
        for (CharData cd : charDataArray) {
            if (cd.cp > r) {
                return cd.chr; // Return the character of the current element
            }
        }
        
        return charDataArray[charDataArray.length - 1].chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
    public String generate(String initialText, int textLength) {
        StringBuilder generatedText = new StringBuilder(initialText);
        while (generatedText.length() < textLength + initialText.length()) {
            String currentWindow = generatedText.substring(Math.max(0, generatedText.length() - windowLength));
            List probs = CharDataMap.get(currentWindow);
            
            // If the current window is not found, break the loop
            if (probs == null) {
                break;
            }
            
            // Get a random character based on the current window's probabilities
            char nextChar = getRandomChar(probs);
            generatedText.append(nextChar); // Append the selected character to the generated text
        }
        
        // Truncate the generated text to the desired length
        return generatedText.substring(initialText.length(), Math.min(generatedText.length(), initialText.length() + textLength));
    }
    

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		// Your code goes here
    }
}
