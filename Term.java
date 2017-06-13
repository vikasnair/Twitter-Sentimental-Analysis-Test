public class Term {
	private String word;
	private int count;

	public Term() {}

	public Term(String word, int count) {
		this.word = word;
		this.count = count;
	}

	public String get_word() {
		return word;
	}

	public void set_word(String word) {
		this.word = word;
	}

	public int get_count() {
		return count;
	}

	public void set_count(int count) {
		this.count = count;
	}
}