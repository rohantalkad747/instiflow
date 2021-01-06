package com.h2o_execution.instiflow;

public class SentimentDetector {
    private NBBOHistory nbboHistory;

    public enum Sentiment {
        BEARISH,
        BULLISH,
        NEUTRAL,
        VERY_BEARISH,
        VERY_BULLISH
    }

    public Sentiment getSentiment(Trade trade) {
        NBBOHistory.NBBO nbbo = nbboHistory.getNBBO(trade.getSymbol(), trade.getExecTime(), trade.getOptionType(), trade.getStrike(), trade.getExpiration());
        return evaluateExecPriceComparedtoNBBO(trade.getOptionType(), trade.getPrice(), nbbo);
    }

    public Sentiment getSentiment(Sweep sweep) {
        NBBOHistory.NBBO nbbo = nbboHistory.getNBBO(sweep.getSymbol(), sweep.getExecTime(), sweep.getOptionType(), sweep.getStrike(), sweep.getExpiration());
        return evaluateExecPriceComparedtoNBBO(sweep.getOptionType(), sweep.getAveragePrice(), nbbo);
    }

    private Sentiment evaluateExecPriceComparedtoNBBO(Trade.OptionType optionType, double execPrice, NBBOHistory.NBBO nbbo) {
        double bestBid = nbbo.getBid().doubleValue();
        double bestOffer = nbbo.getOffer().doubleValue();
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
