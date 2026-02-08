package com.buuz135.simpleclaims.util;

import com.buuz135.simpleclaims.claim.ClaimManager;

public class PartyInactivityThread extends Thread {

    private volatile boolean running = true;

    public PartyInactivityThread() {
        this.setName("PartyInactivityTickingSystem");
        this.setDaemon(true);
    }

    @Override
    public void run() {
        if (!sleepInterruptibly(30 * 1000L)) return;

        while (running) {
            ClaimManager.getInstance().disbandInactiveParties();
            if (!sleepInterruptibly(10 * 60 * 1000L)) return; // Every 10 min
        }
    }

    public void stopThread() {
        if (!running) return;
        running = false;
        this.interrupt();
    }

    private boolean sleepInterruptibly(long millis) {
        try {
            Thread.sleep(millis);
            return running;
        } catch (InterruptedException ignored) {
            running = false;
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
