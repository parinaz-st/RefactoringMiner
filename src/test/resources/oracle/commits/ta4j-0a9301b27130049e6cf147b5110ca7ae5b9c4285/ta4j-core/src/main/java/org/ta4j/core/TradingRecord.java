/*******************************************************************************
 *   The MIT License (MIT)
 *
 *   Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2018 Ta4j Organization 
 *   & respective authors (see AUTHORS)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy of
 *   this software and associated documentation files (the "Software"), to deal in
 *   the Software without restriction, including without limitation the rights to
 *   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *   the Software, and to permit persons to whom the Software is furnished to do so,
 *   subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *   FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.ta4j.core;

import org.ta4j.core.Order.OrderType;
import org.ta4j.core.num.Num;

import java.io.Serializable;
import java.util.List;

import static org.ta4j.core.num.NaN.NaN;


/**
 * A history/record of a trading session.
 * </p>
 * Holds the full trading record when running a {@link Strategy strategy}.
 * It is used to:
 * <ul>
 *     <li>check to satisfaction of some trading rules (when running a strategy)
 *     <li>analyze the performance of a trading strategy
 * </ul>
 */
public interface TradingRecord extends Serializable {

    /**
     * @return the current trade
     */
    Trade getCurrentTrade();
    
    /**
     * Operates an order in the trading record.
     * @param index the index to operate the order
     */
    default void operate(int index) {
        operate(index, NaN, NaN);
    }
    
    /**
     * Operates an order in the trading record.
     * @param index the index to operate the order
     * @param price the price of the order
     * @param amount the amount to be ordered
     */
    void operate(int index, Num price, Num amount);
    
    /**
     * Operates an entry order in the trading record.
     * @param index the index to operate the entry
     * @return true if the entry has been operated, false otherwise
     */
    default boolean enter(int index) {
        return enter(index, NaN, NaN);
    }
    
    /**
     * Operates an entry order in the trading record.
     * @param index the index to operate the entry
     * @param price the price of the order
     * @param amount the amount to be ordered
     * @return true if the entry has been operated, false otherwise
     */
    boolean enter(int index, Num price, Num amount);
    
    /**
     * Operates an exit order in the trading record.
     * @param index the index to operate the exit
     * @return true if the exit has been operated, false otherwise
     */
    default boolean exit(int index) {
        return exit(index, NaN, NaN);
    }
    
    /**
     * Operates an exit order in the trading record.
     * @param index the index to operate the exit
     * @param price the price of the order
     * @param amount the amount to be ordered
     * @return true if the exit has been operated, false otherwise
     */
    boolean exit(int index, Num price, Num amount);
    
    /**
     * @return true if no trade is open, false otherwise
     */
    default boolean isClosed() {
        return !getCurrentTrade().isOpened();
    }
    
    /**
     * @return the recorded trades
     */
    List<Trade> getTrades();
    
    /**
     * @return the number of recorded trades
     */
    default int getTradeCount() {
        return getTrades().size();
    }
    
    /**
     * @return the last trade recorded
     */
    default Trade getLastTrade() {
    	List<Trade> trades = getTrades();
        if (!trades.isEmpty()) {
            return trades.get(trades.size() - 1);
        }
        return null;
    }
    
    /**
     * @return the last order recorded
     */
    Order getLastOrder();
    
    /**
     * @param orderType the type of the order to get the last of
     * @return the last order (of the provided type) recorded
     */
    Order getLastOrder(OrderType orderType);
    
    /**
     * @return the last entry order recorded
     */
    Order getLastEntry();
    
    /**
     * @return the last exit order recorded
     */
    Order getLastExit();

    /**
     * @return the total profit of all the trades
     */
    Num getTotalProfit();

    /**
     * @return the total loss of all the trades
     */
    Num getTotalLoss();

    /**
     * @return the count of the trades that gave profit
     */
    long getProfitTradeCount();

    /**
     * @return the count of the trades that gave loss
     */
    long getLossTradeCount();

    /**
     * @return the count of the trades that gave break even
     */
    long getBreakEvenTradeCount();
}
