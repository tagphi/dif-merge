package com.rtbasia.difmerge.ipfs;

@FunctionalInterface
public interface ProgressHandler {
    void updateProgress(int i, int total);
}
