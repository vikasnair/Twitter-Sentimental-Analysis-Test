// simple class to represent a word in the pre-built dictionary, including fields to represent sentiment complexity

public class Sentiment {
	private String word;
	private int polarity;
	private int power;
	private String type;
	private boolean stemmed;

	// all instances must have these attributes

	public Sentiment(String word, int polarity, int power, String type, boolean stemmed) {
		this.word = word;
		this.polarity = polarity;
		this.power = power;
		this.type = type;
		this.stemmed = stemmed;
	}

	public String get_word() {
		return word;
	}

	public void set_word(String word) {
		this.word = word;
	}

	public int get_polarity() {
		return polarity;
	}

	public void set_polarity(int polarity) {
		this.polarity = polarity;
	}

	public int get_power() {
		return power;
	}

	public void set_power(int power) {
		this.power = power;
	}

	public String get_type() {
		return type;
	}

	public void set_type(String type) {
		this.type = type;
	}

	public boolean get_stemmed() {
		return stemmed;
	}

	public void set_stemmed(boolean stemmed) {
		this.stemmed = stemmed;
	}

	// basic algorithm to assign an int score between -2 and 2

	public int get_evaluation() {
		return polarity * power;
	}
}