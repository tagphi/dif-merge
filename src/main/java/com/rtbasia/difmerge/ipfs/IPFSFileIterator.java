package com.rtbasia.difmerge.ipfs;

import com.rtbasia.difmerge.schedule.MergeTask;
import io.ipfs.multihash.Multihash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class IPFSFileIterator<E> {
    final static Logger logger = LoggerFactory.getLogger(MergeTask.class);

    public Collection<E> someHash;
    public IPFSClient ipfs;

    private CompositHashHandler hashParser;

    public IPFSFileIterator(Collection<E> someHash, IPFSClient ipfsClient) {
        this.someHash = someHash;
        this.ipfs = ipfsClient;
    }

    public IPFSFileIterator<E> mapHash(CompositHashHandler<E> hashParser) {
        this.hashParser = hashParser;

        return this;
    }

    public void forEachLine(LineProcessor<E> lineProcessor, ProgressHandler progressHandler) throws TimeoutException, IOException {
        int i = 0;

        for (E compositHash : someHash) {
            String realHashBase58 = null;

            if (hashParser != null) {
                realHashBase58 = hashParser.extractHash(compositHash);
            } else {
                realHashBase58 = (String)compositHash;
            }

            i++;
            Multihash hash = Multihash.fromBase58(realHashBase58);

            try {
                boolean fileExists = ipfs.fileExists(hash);

                if (!fileExists) {
                    logger.warn("file not exists, this should not happen. hash " + realHashBase58);
                    continue;
                }
            } catch (IOException e) {
                logger.error("error when check file existence, ignore it,  hash " + realHashBase58);

                continue;
            }

            Set<String> blackListData = null;

            progressHandler.updateProgress(i, someHash.size());
            blackListData = new HashSet(Arrays.asList(new String(ipfs.cat(realHashBase58)).split("\n")));

            for (String blackListDatum : blackListData) {
                lineProcessor.process(blackListDatum, compositHash);
            }
        }
    }
}
