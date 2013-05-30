package com.twitter.hbc;

/**
 * This manages all of the reconnection logic.
 */
public interface ReconnectionManager {
    void handleExponentialBackoff();
    void handleLinearBackoff();
    boolean shouldReconnectOn400s();
    int estimateBackfill(double tps);
    void resetCounts();
}
