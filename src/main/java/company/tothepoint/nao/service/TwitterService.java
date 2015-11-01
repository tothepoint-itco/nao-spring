package company.tothepoint.nao.service;

import company.tothepoint.nao.TwitterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.TweetData;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import java.util.ArrayList;
import java.util.List;


public class TwitterService {
    private static final Logger LOG = LoggerFactory.getLogger(TwitterService.class);


    private TwitterConfig twitterConfig;


    String consumerKey;
    String consumerSecret;
    String accessToken;
    String accessTokenSecret;
    Twitter twitter;


    public TwitterService() {
        twitterConfig = new TwitterConfig();
        consumerKey = twitterConfig.getConsumerKey(); // The application's consumer key
        consumerSecret = twitterConfig.getConsumerSecret(); // The application's consumer secret
        accessToken = twitterConfig.getAccessToken(); // The access token granted after OAuth authorization
        accessTokenSecret = twitterConfig.getAccessTokenSecret(); // The access token secret granted after OAuth authorization

        twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
    }

    public long sendTweet(String tweetContent) {
        LOG.debug("Method sendTweet("+tweetContent+") called!");
        TweetData twd = new TweetData(tweetContent);
        Tweet tweet = twitter.timelineOperations().updateStatus(twd);
        return tweet.getId();
    }

    public List<String> getTweetsFrom(String author) {
        LOG.debug("Method getTweetsFrom("+author+") called!");
        List<Tweet> tweets = twitter.timelineOperations().getUserTimeline(author);
        List<String> resultList = new ArrayList<>();
        for (Tweet t : tweets) {
            resultList.add(t.getText());
        }
        return resultList;
    }



}
