package com.rtbasia.difmerge.ipfs;

import com.rtbasia.difmerge.schedule.MergeTask;
import io.ipfs.multihash.Multihash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class IPFSFileIterator {
    final static Logger logger = LoggerFactory.getLogger(MergeTask.class);

    public Collection<String> someHash;
    public IPFSClient ipfs;

    public IPFSFileIterator(Collection<String> someHash, IPFSClient ipfsClient) {
        this.someHash = someHash;
        this.ipfs = ipfsClient;
    }

    public void forEachLine(LineProcessor lineProcessor, ProgressHandler progressHandler) throws TimeoutException, IOException {
        int i = 0;

        for (String hashBase58 : someHash) {
            i++;
            Multihash hash = Multihash.fromBase58(hashBase58);

            try {
                boolean fileExists = ipfs.fileExists(hash);

                if (!fileExists) {
                    logger.warn("file not exists, this should not happen. hash " + hashBase58);
                    continue;
                }
            } catch (IOException e) {
                logger.error("error when check file existence, ignore it,  hash " + hashBase58);

                continue;
            }

            Set<String> blackListData = null;

            progressHandler.updateProgress(i, someHash.size());
            blackListData = new HashSet(Arrays.asList(new String(ipfs.cat(hashBase58)).split("\n")));

            for (String blackListDatum : blackListData) {
                lineProcessor.process(blackListDatum, hashBase58);
            }
        }
    }
}
