package com.rtbasia.difmerge.ipfs;


@FunctionalInterface
public interface LineProcessor<E> {
    void process(String line, E compositHash);
}
