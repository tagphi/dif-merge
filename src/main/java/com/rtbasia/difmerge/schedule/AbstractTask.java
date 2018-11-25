package com.rtbasia.difmerge.schedule;

public abstract class AbstractTask implements Runnable {
    protected ProgressLisener progressLisener;

    protected void beginStep(String progress) {
        if (progressLisener != null) {
            progressLisener.onStart(progress);
        }
    }

    protected void onError(String message) {
        if (progressLisener != null) {
            progressLisener.onError(message);
        }
    }

    protected void endStep() {
        if (progressLisener != null) {
            progressLisener.onComplete();
        }
    }

    public abstract void run();

    public void addProgressLisener(ProgressLisener progressLisener) {
        this.progressLisener = progressLisener;
    }
}
