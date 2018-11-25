package com.rtbasia.difmerge.schedule;

public interface ProgressLisener {
    void onStart(String step);
    void onError(String message);
    void onComplete();
}
