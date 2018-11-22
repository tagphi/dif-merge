package com.rtbasia.difmerge.ipfs;


@FunctionalInterface
public interface MergeHandler<E> {
    E merge(String line, String hash);
}
