public boolean process() throws IOException, InterruptedException {
    long stopTime = System.currentTimeMillis() + this.getMessageReadTimeout();
    String msg = processNextMessage();
    while (msg == null && System.currentTimeMillis() < stopTime) {
        msg = processNextMessage();
    }
    if (msg != null) {
        return this.queue.offer(msg, offerTimeoutMillis, TimeUnit.MILLISECONDS);
    } else {
        return false;
    }
}
