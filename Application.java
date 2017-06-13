import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterException;

public class Application {
	public static final Object lock = new Object();
	public static Scanner input = new Scanner(System.in);
	public static char selection;
	public static Date date = new Date();
	public static DateFormat date_formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static DecimalFormat formatter = new DecimalFormat("#.##");
	public static Dictionary dictionary = new Dictionary();
	public static List<String> negatives, intensifiers;

	public static void main(String[] args) {
		System.out.println("\nWelcome to the Twitter Sentiment Analysis Tool.");
		menu();
	}

	// a menu method which prompts user for input

	public static void menu() {

		String user_query;
		int size;
		List<Status> statuses;
		List<String> statuses_text;
		List<Hashtag> hashtags;
		double result;

		while (true) {
			user_query = "";
			size = 0;
			statuses = new ArrayList<>();
			statuses_text = new ArrayList();
			result = 10101010;

			System.out.print("(1) Fetch new tweets or (2) analyze existing data: "); selection = validate(false);

			if (selection == '1') {
				System.out.print("\nEnter a topic to analyze: "); user_query = input.nextLine();
				System.out.print("Enter how many tweets you would like to collect (>99): "); size = input.nextInt(); input.nextLine();

				while (size < 100) {
					System.out.print("Not a large enough sample size. Choose at least 100: "); 
					size = input.nextInt(); input.nextLine();				
				}

				System.out.print("(1) Fetch real-time tweets or (2) from the past week: "); selection = validate(false);

				if (selection == '1') {
					System.out.print("(1) Mine to file (recommended for 1000+ tweets) or (2) fetch and analyze stream: "); selection = validate(false);

					if (selection == '1') {
						mine(user_query, size);
						return;
					}

					else
						statuses = fetch_stream(user_query, size);
				}

				else
					statuses = fetch_past(user_query, size);
				System.out.println();

				for (Status status : statuses)
					statuses_text.add(status.getText());
				result = analyze(statuses_text, user_query, size);
			}

			else {
				System.out.print("Please enter the name of the CSV file containing tweet data (without extension): "); String file = input.nextLine();
				statuses_text = fetch_mine(file);

				while (statuses_text == null) {
					System.out.print("\nCouldn't find file, try again: "); file = input.nextLine();
					statuses_text = fetch_mine(file);
				}

				user_query = file;
				result = analyze(statuses_text, file, size);
			}

			if (result != 10101010) {
				hashtags = get_popular_hashtags(statuses_text);
				print_hashtags(hashtags);

				String f_result = formatter.format(result);
				double score = (result * 100) / 4;
				String f_score = formatter.format(score);

				System.out.print("The sentiment score for " + user_query + " is: " + result + ".\nThis is on a scale of -4 (overwhelmingly negative) to 4 (overwhelmingly positive).");
				System.out.print("\n\nIt seems the sentiment of " + user_query + " is ");
				
				if (score < -25)
					System.out.print("overwhelmingly negative");
				else if (score < -10)
					System.out.print("fairly negative");
				else if (score < 10)
					System.out.print("relatively neutral");
				else if (score < 25)
					System.out.print("fairly positive");
				else
					System.out.print("overwhelmingly positive");

				System.out.println(" at " + f_score + "% positivity.");
				System.out.print("\nExit (y/n): "); selection = validate(true);

				if (selection == 'y')
					break;
				else
					continue;
			}

			else
				System.out.println("There was an error. Try again.");
		}
	}

	public static double get_sentiment(String[] status_split, String user_query) {
		// variables for individual sentiment; weighted by retweets/favorites, averaged by number of words containing sentiment data

		double status_sentiment = 0;
		int sentiment_count = 0;

		// search through the dictionary
		// for each status, if a word in the dictionary appears in the current status (and not in the query), then add its evaluation (representing a subjective net negativity / positivity score)

		for (int i = 0; i < status_split.length; i++) {
			for (Sentiment sentiment : dictionary.dictionary) {
				if (!user_query.contains(sentiment.get_word()) && (status_split[i].equals(sentiment.get_word()) || (sentiment.get_stemmed() && status_split[i].contains(sentiment.get_word())))) { //  || status_split[i].contains(sentiment.get_word())
					if (i > 0) {
						if (negatives.contains(status_split[i - 1])) {
							if (i > 1 && intensifiers.contains(status_split[i - 2]))
								status_sentiment -= (sentiment.get_evaluation() * 2); // * uppercase
							else if (i == 1)
								status_sentiment -= (sentiment.get_evaluation());
						}

						else if (intensifiers.contains(status_split[i - 1]))
							status_sentiment += (sentiment.get_evaluation() * 2);
						else
							status_sentiment += sentiment.get_evaluation();
					}

					else
						status_sentiment += (sentiment.get_evaluation());
					sentiment_count++;
				}
			}
		}

		if (sentiment_count != 0)
			return status_sentiment / sentiment_count;
		else
			return 10101010;
	}

	// bulk of the program, which analyzes a given query

	public static double analyze(List<String> statuses_text, String user_query, int size) {

		// create a list to store fetched tweets + fetched tweets with scores

		List<String> scores = new ArrayList<>();

		// create arrays of words which negate and intensify sentiment

		negatives = Arrays.asList("no", "not", "never", "hardly", "neither", "nor", "none", "nobody", "nowhere", "nothing", "rarely", "isnt", "aint", "dont", "couldnt", "shouldnt", "wasnt",
			"hasnt", "without", "havent", "few");
		intensifiers = Arrays.asList("amazingly", "astoundingly", "awful", "bare", "bloody", "crazy", "dead", "dreadfully", "colossally", "especially", "exceptionally", "excessively", "extremely",
			"extraordinarily", "fantastically", "frightfully", "fucking", "fully", "hella", "holy", "incredibly", "insanely", "literally", "mad", "mightily", "moderately", "most", "outrageously", "phenomenally",
			"precious", "quite", "radically", "rather", "real", "really", "remarkably", "right", "sick", "so", "somewhat", "strikingly", "super", "supremely", "surpassingly", "terribly", "terrifically", "too",
			"totally", "uncommonly", "unusually", "veritable", "very", "wicked", "many", "all", "alot", "bigger", "more", "biggest", "larger", "largest");

		// populate pre-built dictionary of words and corresponding sentiment complexities (see Dictionary.java)

		dictionary.populate();

		boolean write = false;
		System.out.print("\nWrite results to file (y/n): "); selection = validate(true);
		
		if (selection == 'y' || selection == 'Y')
			write = true;

		// variables for total sentiment, statuses which contained sentiment data, and minimum sentiment score (to adjust data curve at end)
		
		double avg_total_sentiment;
		double total_sentiment = 0;
		int status_count = 0;

		// search through tweet objects

		for (String status : statuses_text) {

			// rebuild the tweet text, ignoring special characters and punctuation, and split into a list for easy iteration

			String[] status_split = status.replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ").toLowerCase().split(" ");
			double status_sentiment = get_sentiment(status_split, user_query);
			
			if (status_sentiment != 10101010)
				status_count++;
			else
				continue;
			
			// add resulting data to scores list

			double percent_status_sentiment = (status_sentiment * 100) / 4;
			String f_percent_status_sentiment = formatter.format(percent_status_sentiment);
			String f_status = Arrays.toString(status_split);

			StringBuilder builder = new StringBuilder();
			builder.append(f_percent_status_sentiment + "%");
			builder.append(",");
			builder.append("\"" + f_status + "\"");
			builder.append("\n");
			scores.add(builder.toString());

			total_sentiment += status_sentiment;
		}

		if (status_count != 0) {
			avg_total_sentiment = total_sentiment / status_count;

			if (write) {
				try {
					String f_user_query = String.join("_", user_query.split(" "));
					char[] date_array = date_formatter.format(date).toCharArray();
					String f_date = "";
					
					for (char c : date_array) {
						if (Character.isDigit(c))
							f_date += c;
						else if (c == '/')
							f_date += '-';
						else
							f_date += "_";
					}

					String file_name = "results/" + f_user_query + "_" + f_date + ".csv";
					PrintWriter writer = new PrintWriter(new File(file_name));
					double percent_total_sentiment = (avg_total_sentiment * 100) / 4;
					String f_percent_total_sentiment = formatter.format(percent_total_sentiment);
					writer.write("Positivity:," + f_percent_total_sentiment + "%\n"); // - lowest_sentiment

					for (String score : scores) {
						writer.write(score); 
						writer.flush();
					}

					writer.close();
				}

				catch (FileNotFoundException ex) {
					System.out.println("Error writing to file.");
					ex.printStackTrace();

					// give user the option to print all tweets analyzed

					System.out.print("\nPrint all tweets (y/n): "); selection = validate(true);

					if (selection == 'y')
						print(scores);
				}
			}

			return avg_total_sentiment; // - lowest_sentiment;
		}

		else
			return 10101010;
	}

	public static Twitter get_past() {

		// connect to Twitter API

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("####");
		cb.setOAuthConsumerSecret("####");
		cb.setOAuthAccessToken("####");
		cb.setOAuthAccessTokenSecret("####");

		return new TwitterFactory(cb.build()).getInstance();
	}

	public static TwitterStream get_stream() {

		// connect to Twitter API

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey("####");
		cb.setOAuthConsumerSecret("####");
		cb.setOAuthAccessToken("####");
		cb.setOAuthAccessTokenSecret("####");

		return new TwitterStreamFactory(cb.build()).getInstance();
	}

	public static List<Status> fetch_past(String user_query, int size) {
		
		Twitter twitter = get_past();
		List<Status> statuses = new ArrayList<>();

		try {

			// generate a query, restrict by language and allow 100 results per page

			String[] keywords = user_query.split(" ");
			String keywords_rebuilt = "" + keywords[0];

			for (String keyword : Arrays.copyOfRange(keywords, 1, keywords.length))
				keywords_rebuilt += (" OR " + keyword);

			Query query = new Query(keywords_rebuilt);
			query.setLang("en");
			query.setCount(100);
			query.setSince("2017-04-28");
			// query.setUntil("2017-04-29");
			QueryResult result = twitter.search(query);

			// recurringly add tweets to list, and generate new page (as long as there is a next page and counter has not reached its limit)

			int count = 0;

			while (result.hasNext() && count < size / 10) {
				for (Status status : result.getTweets())

					// exclude retweets and links

					if (!status.isRetweet() && status.getURLEntities().length == 0)
						statuses.add(status);

				query = result.nextQuery();
				result = twitter.search(query);
				count++;
			}
		}

		catch (TwitterException ex) {
			ex.printStackTrace();
		}

		finally {
			return statuses;
		}
	}

	public static List<Status> fetch_stream(String user_query, int size) {
		
		System.out.println();
		
		// initialize the Twitter stream
		
		List<Status> statuses = new ArrayList<>();
		TwitterStream twitter_stream = get_stream();
		StatusListener listener = new StatusListener() {

			public void onStatus(Status status) {
				if (!status.isRetweet() && status.getURLEntities().length == 0)
					statuses.add(status);
				if (statuses.size() > size) {
					synchronized (lock) {
						lock.notify();
					}
				}
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// System.out.println("Track limitation notice:" + numberOfLimitedStatuses);
			}

			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			public void onStallWarning(StallWarning sw) {
				System.out.println(sw.getMessage());
			}	
		};

		FilterQuery filter = new FilterQuery();
		String[] language = { "en" };
		String[] keywords = user_query.split(" ");
		filter.track(keywords);
		filter.language(language);
		twitter_stream.addListener(listener);
		twitter_stream.filter(filter);

		try {
			synchronized (lock) {
				lock.wait();
			}
		}

		catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		twitter_stream.shutdown();

		return statuses;
	}

	public static List<String> fetch_mine(String file) {
		List<String> statuses_text = new ArrayList<>();

		try {
			Scanner reader = new Scanner(new File("mined_results/" + file + ".csv"));
			StringBuilder builder;

			while (reader.hasNextLine()) {
				builder = new StringBuilder();
				builder.append(reader.nextLine());

				while (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\"' && reader.hasNextLine())
					builder.append(reader.nextLine());
				statuses_text.add(builder.toString());
			}
		}

		catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return null;
		}

		statuses_text.remove(0); statuses_text.remove(0);
		
		return statuses_text;
	}

	public static void mine(String user_query, int size) {
		System.out.println();
		
		// initialize the Twitter stream
		
		List<Status> statuses = new ArrayList<>();
		TwitterStream twitter_stream = get_stream();

		char[] date_array = date_formatter.format(date).toCharArray();
		String f_date = "";
		
		for (char c : date_array) {
			if (Character.isDigit(c))
				f_date += c;
			else if (c == '/')
				f_date += '-';
			else
				f_date += "_";
		}

		try {
			String file_name = "mined_results/" + user_query + ".csv";
			PrintWriter writer = new PrintWriter(new File(file_name));
			writer.write("Query:," + user_query.toString() + "\n");
			writer.write("Date:," + f_date + "\n");

			StatusListener listener = new StatusListener() {

				public void onStatus(Status status) {
					if (!status.isRetweet() && status.getURLEntities().length == 0) {
						StringBuilder builder = new StringBuilder();
						builder.append("\"" + status.getText() + "\""); builder.append(",\n");
						writer.write(builder.toString());
						writer.flush();
					}

					if (statuses.size() > size) {
						synchronized (lock) {
							lock.notify();
						}
					}
				}

				public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
					System.out.println("Status deletion notice id:" + statusDeletionNotice.getStatusId());
				}

				public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
					// System.out.println("Track limitation notice:" + numberOfLimitedStatuses);
				}

				public void onScrubGeo(long userId, long upToStatusId) {
					System.out.println("Scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
				}

				public void onException(Exception ex) {
					ex.printStackTrace();
				}

				public void onStallWarning(StallWarning sw) {
					System.out.println(sw.getMessage());
				}	
			};

			FilterQuery filter = new FilterQuery();
			String[] language = { "en" };
			String[] keywords = user_query.split(" ");
			filter.track(keywords);
			filter.language(language);
			twitter_stream.addListener(listener);
			twitter_stream.filter(filter);

			try {
				synchronized (lock) {
					lock.wait();
				}
			}

			catch (InterruptedException ex) {
				ex.printStackTrace();
				writer.close();
			}

			twitter_stream.shutdown();
		}

		catch (FileNotFoundException ex) {
			System.out.println("Error writing to file.");
			ex.printStackTrace();
		}
	}

	public static void print(List<String> strings) {
		for (String string : strings)
			System.out.println(string);
	}

	public static void print_hashtags(List<Hashtag> hashtags) {

		System.out.println();

		for (int i = 0; i < hashtags.size(); i++) {
			int k = i;

			for (int j = i + 1; j < hashtags.size(); j++) {
				if (hashtags.get(j).get_count() < hashtags.get(k).get_count()) {
					Hashtag temp = hashtags.get(k);
					hashtags.set(k, hashtags.get(j));
					hashtags.set(j, temp);
				}
			}
		}

		if (hashtags.size() > 5)
			for (int i = 0; i < 5; i++)
				if (hashtags.get(i) != null && hashtags.get(i).get_count() > 1)
					System.out.print(hashtags.get(i).get_hashtag() + " ");
	}

	public static List<Hashtag> get_popular_hashtags(List<String> statuses_text) {
		
		List<Hashtag> hashtags = new ArrayList<>();

		for (String status_text : statuses_text) {
			if (status_text.contains("#")) {

				String current_hashtag = "";
				boolean on_hashtag = false;

				for (char c : status_text.toCharArray()) {
					if (c == '#') {
						current_hashtag += c;
						on_hashtag = true;
					}

					else if (on_hashtag && !Character.isLetter(c)) {
						boolean hashtag_exists = false;

						for (Hashtag hashtag : hashtags) {
							if (current_hashtag.equals(hashtag.get_hashtag())) {
								hashtag_exists = true;
								hashtag.set_count(hashtag.get_count() + 1);
							}
						}

						if (!hashtag_exists)
							hashtags.add(new Hashtag(current_hashtag, 1));
						current_hashtag = "";
						on_hashtag = false;
					}

					else if (on_hashtag)
						current_hashtag += c;
				}
			}
		}

		return hashtags;
	}

	public static char validate(boolean y_n) {
		char selection = input.nextLine().toLowerCase().charAt(0);

		if (y_n) {
			while (selection != 'y' && selection != 'n') {
				System.out.print("\nSorry! Couldn't understand you. (y/n): "); selection = input.nextLine().toLowerCase().charAt(0);
			}
		}

		else {
			while (selection != '1' && selection != '2') {
				System.out.print("\nSorry! Couldn't understand you. (1/2): "); selection = input.nextLine().toLowerCase().charAt(0);
			}
		}
	

		return selection;
	}
}