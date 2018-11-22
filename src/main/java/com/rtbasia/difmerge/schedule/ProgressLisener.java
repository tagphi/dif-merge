package com.rtbasia.difmerge.schedule;

public interface ProgressLisener {
    void onProgress(String step, String status, String message);
}
