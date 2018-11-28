package com.rtbasia.difmerge.ipfs;

@FunctionalInterface
public interface  CompositHashHandler<T> {
    String extractHash(T compositHash);
}