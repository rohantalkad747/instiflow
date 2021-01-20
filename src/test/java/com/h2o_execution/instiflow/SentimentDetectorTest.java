package com.h2o_execution.instiflow;

import com.h2o_execution.instiflow.SentimentDetector.Sentiment;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SentimentDetectorTest {

    SentimentDetector sentimentDetector = new SentimentDetector();
    Sweep.SweepBuilder sweepBuilder = sampleSweep();


    @Test
    void whenCallAtBid_ThenShouldReturnBearishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.CALL).averagePrice(124).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.BEARISH));
    }

    @Test
    void whenCallBelowBid_ThenShouldReturnVeryBearishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.CALL).averagePrice(123).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.VERY_BEARISH));
    }

    @Test
    void whenCallAtAsk_ThenShouldReturnBullishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.CALL).averagePrice(125).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.BULLISH));
    }

    @Test
    void whenCallAboveAsk_ThenShouldReturnVeryBullishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.CALL).averagePrice(126).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.VERY_BULLISH));
    }

    @Test
    void whenPutAtBid_ThenShouldReturnBullishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.PUT).averagePrice(124).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.BULLISH));
    }

    @Test
    void whenPutBelowBid_ThenShouldReturnVeryBullishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.PUT).averagePrice(123).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.VERY_BULLISH));
    }

    @Test
    void whenPutAtAsk_ThenShouldReturnBearishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.PUT).averagePrice(125).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.BEARISH));
    }

    @Test
    void whenPutAboveAsk_ThenShouldReturnVeryBearishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.PUT).averagePrice(126).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.VERY_BEARISH));
    }

    private Sweep.SweepBuilder sampleSweep() {
        return Sweep.builder()
                .expiration("1-5-2020")
                .execTime(new Date().getTime())
                .bestBid(124)
                .bestOffer(125)
                .symbol("AAPL")
                .strike(130)
                .type(Sweep.ExecutionType.INTER_MARKET)
                .cashAmount(125_000);
    }
}