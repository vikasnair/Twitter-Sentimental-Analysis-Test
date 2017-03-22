// class to represent a dictionary of words and their respective sentiment complexity

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Dictionary {

	// array list to store word obects

	public ArrayList<Sentiment> dictionary = new ArrayList<>();

	public Dictionary() {}
	
	// method to populate the dictionary, reads from file in directory

	public void populate() {
		String file = "subjectivity_dictionary.txt";
		String line = null;

		try {
			FileReader file_reader = new FileReader(file);
			BufferedReader buffer_reader = new BufferedReader(file_reader);

			StringTokenizer token = null;

			// read line-by-line

			while ((line = buffer_reader.readLine()) != null) {

				// create a token to iterate through each field per-line

				token = new StringTokenizer(line, " ");

				while(token.hasMoreTokens()) {

					// gather each field, then assign values to represent weak/strong or negative positive
					// so far I am using a very basic calculation: -1 if the word has negative connotation, 0 if neutral, 1 if positive || 1 if weak connotation, 2 if strong

					String s_power = token.nextToken(); s_power = s_power.substring(5, s_power.length());
					int power = 0;

					if (s_power.equals("weaksubj")) {
						power = 1;
					}

					else if (s_power.equals("strongsubj")) {
						power = 2;
					}
					
					String trash = token.nextToken();
					
					String word = token.nextToken(); word = word.substring(6, word.length());

					String type = token.nextToken(); type = type.substring(5, type.length());

					String s_stemmed = token.nextToken(); s_stemmed = s_stemmed.substring(9, s_stemmed.length());
					boolean stemmed = false;

					if (s_stemmed.equals("y")) {
						stemmed = true;
					}

					String s_polarity = token.nextToken(); s_polarity = s_polarity.substring(14, s_polarity.length());
					int polarity = 0;

					if (s_polarity.equals("negative")) {
						polarity = -1;
					}

					else if (s_polarity.equals("positive")) {
						polarity = 1;
					}

					// construct and add

					Sentiment new_sentiment = new Sentiment(word, polarity, power, type, stemmed);
					dictionary.add(new_sentiment);
				}
			}

			buffer_reader.close();
		}

		catch (FileNotFoundException ex) {
			System.out.println("Unable to open file.");
		}

		catch (IOException ex) {
			System.out.println("Error reading file.");
		}
	}
}