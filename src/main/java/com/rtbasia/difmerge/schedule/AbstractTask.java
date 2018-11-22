package com.rtbasia.difmerge.schedule;

public abstract class AbstractTask implements Runnable {
    protected ProgressLisener progressLisener;

    protected void progress(String progress, String status, String message) {
        if (progressLisener != null) {
            progressLisener.onProgress(progress, status, message);
        }
    }

    public abstract void run();

    public void addProgressLisener(ProgressLisener progressLisener) {
        this.progressLisener = progressLisener;
    }
}
