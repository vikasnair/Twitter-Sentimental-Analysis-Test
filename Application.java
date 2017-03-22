import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterException;

public class Application {
	public static void main(String[] args) {

		System.out.println("\nWelcome to the Twitter Sentiment Analysis Tool.");

		menu();
	}

	// a menu method which prompts user for input

	public static void menu() {
		Scanner input = new Scanner(System.in);

		while (true) {
			System.out.print("\nPlease enter a topic to analyze: "); 
			String user_query = input.nextLine();

			System.out.println("\n\nThe sentiment score for " + user_query + " is: " + analyze(user_query));

			System.out.print("\nExit (y/n)? "); String prompt = input.nextLine();

			while (prompt.charAt(0) != 'y' && prompt.charAt(0) != 'n') {
				System.out.print("\nSorry! Couldn't understand you. Print all tweets (y/n)? "); prompt = input.nextLine();	
			}

			if (prompt.charAt(0) == 'y') {
				break;
			}

			else {
				continue;
			}
		}
	}

	// bulk of the program, which analyzes a given query

	public static int analyze(String user_query) {
		Scanner input = new Scanner(System.in);

		try {

			// connect to Twitter API

			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true);
			cb.setOAuthConsumerKey("####");
			cb.setOAuthConsumerSecret("####");
			cb.setOAuthAccessToken("####");
			cb.setOAuthAccessTokenSecret("####");

			// create a Twitter object

			Twitter twitter = new TwitterFactory(cb.build()).getInstance();

			// create a new dictionary object, and populate it with a pre-built dictionary of words and corresponding sentiment complexities (see Dictionary.java)

			Dictionary dictionary = new Dictionary(); dictionary.populate();

			// create an array to store fetched tweets

			List<Status> statuses = new ArrayList<>();

			// generate a query, restrict by language and allow 100 results per page

			Query query = new Query(user_query);
			query.setLang("en");
			query.setCount(100);

			QueryResult result = twitter.search(query);

			// recurringly add tweets to list, and generate new page (as long as there is a next page and counter has not reached its limit)

			int count = 0;

			while (result.hasNext() && count < 32) { // 320 max
				for (Status status : result.getTweets()) {

					// exclude retweets and links

					if (!status.isRetweet() && !status.getText().toLowerCase().contains("http")) {

						// if the status is a quote and the quote has not already been added, then add the quote

						if (status.getText().contains("\"") && status.getQuotedStatus() != null && !statuses.contains(status.getQuotedStatus())) {
							statuses.add(status.getQuotedStatus());
						}

						else {
							statuses.add(status);
						}
					}
				}

				query = result.nextQuery();
				result = twitter.search(query);

				count++;
			}

			int total_sentiment = 0;

			for (Status status : statuses) {

				// search through the dictionary
				// for each status, if a word in the dictionary appears in the current status, then add its evaluation (representing a subjective net negativity / positivity score)

				for (Sentiment sentiment : dictionary.dictionary) {
					if (status.getText().contains(sentiment.get_word())) {
						total_sentiment += sentiment.get_evaluation();
					}
				}
			}

			// give user the option to print all tweets analyzed

			System.out.print("\nPrint all tweets (y/n)? "); String prompt = input.nextLine();

			while (prompt.charAt(0) != 'y' && prompt.charAt(0) != 'n') {
				System.out.print("\nSorry! Couldn't understand you. Print all tweets (y/n)? "); prompt = input.nextLine();	
			}

			if (prompt.charAt(0) == 'y') {
				print(statuses);
			}

			return total_sentiment;
		}

		catch (TwitterException ex) {
			System.out.println("Can't reach Twitter. Check your internet. If that's fine, then try again in 15 minutes.");
		}

		return 0;
	}

	// a simple method to print relevant information per status

	public static void print(List<Status> statuses) {
		for (Status status : statuses) {
			System.out.println("@" + status.getUser().getScreenName() + ": " + status.getText() + "\n");
		}
	}
}