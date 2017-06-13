# Twitter Sentiment Analysis Test

An experiment which takes a query, searches recent tweets related to the topic, and assigns a score representing overall sentiment towards the topic. A negative number is negative sentiment. A positive number is positive sentiment.

This program is in its nascency right now. This is an on-going learning project I took on to get a better understanding of how to work with APIs. As such, the algorithm designed is extremely basic and there are a number of improvements that can be made. Still, the evaluations seem to be fairly accurate. Try searching your company. Or try searching for political topics such as "trump russia fbi," or cultural topics such as "oscars."

Later this test could be applied to different industries. One could gather data representing the public’s favorability towards a given stock, or towards a movie out in theaters, and so on.

Instructions:

**Compile:**
javac -cp twitter4j-core-4.0.4.jar:twitter4j-stream-4.0.4.jar *.java
NOTE: do not re-compile. Doing so will override the API configuration.

**Run:**
java -cp .:twitter4j-core-4.0.4.jar:twitter4j-stream-4.0.4.jar Application