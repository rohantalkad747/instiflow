package com.h2o_execution.instiflow;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SentimentDetector {

    public enum Sentiment {
        BEARISH,
        BULLISH,
        NEUTRAL,
        VERY_BEARISH,
        VERY_BULLISH
    }

    public Sentiment getSentiment(Trade trade) {
        return evaluateExecPriceComparedtoNBBO(trade.getOptionType(), trade.getPrice(), trade.getBestBid(), trade.getBestOffer());
    }

    public Sentiment getSentiment(Sweep sweep) {
        return evaluateExecPriceComparedtoNBBO(sweep.getOptionType(), sweep.getAveragePrice(), sweep.getBestBid(), sweep.getBestOffer());
    }

    private Sentiment evaluateExecPriceComparedtoNBBO(Trade.OptionType optionType, double execPrice, double bestBid, double bestOffer) {
        if (optionType == Trade.OptionType.CALL) {
            return evaluateCallSentiment(bestBid, bestOffer, execPrice);
        }
        return evaluatePutSentiment(bestBid, bestOffer, execPrice);
    }

    private Sentiment evaluatePutSentiment(double bestBid, double bestOffer, double execPrice) {
        Sentiment sentiment;
        if (execPrice == bestOffer) {
            sentiment = Sentiment.BEARISH;
        } else if (execPrice > bestOffer) {
            sentiment = Sentiment.VERY_BEARISH;
        } else if (execPrice == bestBid) {
            sentiment = Sentiment.BULLISH;
        } else if (execPrice < bestBid) {
            sentiment = Sentiment.VERY_BULLISH;
        } else {
            sentiment = Sentiment.NEUTRAL;
        }
        return sentiment;
    }

    private Sentiment evaluateCallSentiment(double bestBid, double bestOffer, double execPrice) {
        Sentiment sentiment;
        if (execPrice == bestOffer) {
            sentiment = Sentiment.BULLISH;
        } else if (execPrice > bestOffer) {
            sentiment = Sentiment.VERY_BULLISH;
        } else if (execPrice == bestBid) {
            sentiment = Sentiment.BEARISH;
        } else if (execPrice < bestBid) {
            sentiment = Sentiment.VERY_BEARISH;
        } else {
            sentiment = Sentiment.NEUTRAL;
        }
        return sentiment;
    }
}
