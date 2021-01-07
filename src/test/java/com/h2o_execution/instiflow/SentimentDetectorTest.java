package com.h2o_execution.instiflow;

import com.h2o_execution.instiflow.SentimentDetector.Sentiment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SentimentDetectorTest {

    NBBOHistory hist = mock(NBBOHistory.class);
    SentimentDetector sentimentDetector = new SentimentDetector(hist);
    Sweep.SweepBuilder sweepBuilder = sampleSweep();
    NBBOHistory.NBBO nbbo = new NBBOHistory.NBBO(BigDecimal.valueOf(124), BigDecimal.valueOf(125));

    @BeforeEach
    public void initMocks() {
        when(hist.getNBBO(any(), any(), any(), any(), any())).thenReturn(nbbo);
    }


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

    // puts

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
    void whenCallAboveAsk_ThenShouldReturnVeryBearishSentiment() {
        Sweep sweep = sweepBuilder.optionType(Trade.OptionType.PUT).averagePrice(126).build();
        Sentiment sentiment = sentimentDetector.getSentiment(sweep);
        assertThat(sentiment, equalTo(Sentiment.VERY_BEARISH));
    }

    private Sweep.SweepBuilder sampleSweep() {
        return Sweep.builder()
                .expiration("1-5-2020")
                .execTime(new Date().getTime())
                .symbol("AAPL")
                .strike(BigDecimal.valueOf(130))
                .type(Sweep.ExecutionType.INTER_MARKET)
                .cashAmount(125_000);
    }
}