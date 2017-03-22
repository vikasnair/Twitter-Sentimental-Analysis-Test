# Twitter Sentiment Analysis Test

An experimental program which takes a query, searches recent tweets related to the topic, and assigns a score representing overall sentiment towards the topic. A negative number is negative sentiment. A positive number is positive sentiment.
This program is in its nascency right now. This is an on-going learning project I took on to get a better understanding of how to work with APIs. As such, the algorithm designed is extremely basic and there are a number of improvements that can be made.

Later the test could be applied to different industry. One could gather data representing the public’s favorability towards a given stock, or towards a movie out in theaters, and so on.

Instructions:

**Compile:**
javac -cp twitter4j-core-4.0.4.jar *.java

**Run:**
java -cp .:twitter4j-core-4.0.4.jar Application