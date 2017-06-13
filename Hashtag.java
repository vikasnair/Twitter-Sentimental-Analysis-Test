public class Hashtag {
	private String hashtag;
	private int count;

	public Hashtag() {}

	public Hashtag(String hashtag, int count) {
		this.hashtag = hashtag;
		this.count = count;
	}

	public String get_hashtag() {
		return hashtag;
	}

	public void set_hashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	public int get_count() {
		return count;
	}

	public void set_count(int count) {
		this.count = count;
	}
}