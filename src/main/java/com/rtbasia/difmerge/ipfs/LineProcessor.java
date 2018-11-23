package com.rtbasia.difmerge.ipfs;


@FunctionalInterface
public interface LineProcessor<E> {
    E process(String line, String hash);
}
