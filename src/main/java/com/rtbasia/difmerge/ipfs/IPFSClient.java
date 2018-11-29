package com.rtbasia.difmerge.ipfs;

import com.rtbasia.difmerge.aop.PerformanceLog;
import com.rtbasia.difmerge.schedule.DeltaUploadTask;
import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class IPFSClient {
    final static Logger logger = LoggerFactory.getLogger(IPFSClient.class);

    private IPFS ipfs;
    private int defaultCheckExistsTimeoutSecs;
    private int defaultDownloadTimeoutSecs;

    public IPFSClient(String host, int port, int defaultCheckExistsTimeoutSecs, int defaultDownloadTimeoutSecs) {
        String multiAddr = String.format("/ip4/%s/tcp/%d", host, port);

        this.ipfs = new IPFS(multiAddr);

        this.defaultCheckExistsTimeoutSecs = defaultCheckExistsTimeoutSecs;
        this.defaultDownloadTimeoutSecs = defaultDownloadTimeoutSecs;
    }

    @PerformanceLog
    public MerkleNode uploadFile(String path) throws IOException {
        return this.ipfs.add(new NamedStreamable.FileWrapper(new File(path))).get(0);
    }

    @PerformanceLog
    public MerkleNode upload(byte[] data) throws  IOException {
        return this.ipfs.add(new NamedStreamable.ByteArrayWrapper(data)).get(0);
    }

    @PerformanceLog
    public MerkleNode upload(String data) throws IOException {
        return this.upload(data.getBytes());
    }

    public boolean fileExists(Multihash hash)  throws IOException {
        return fileExists(hash, defaultCheckExistsTimeoutSecs, TimeUnit.SECONDS); // 默认等待3秒
    }

    @PerformanceLog
    public boolean fileExists(Multihash hash, int timeout, TimeUnit unit) throws IOException {
        ExecutorService exec = Executors.newSingleThreadExecutor();

        final Callable<List<MerkleNode>> call = () -> {
            List<MerkleNode> nodes = this.ipfs.ls(hash);

            return nodes;
        };

        boolean exists = false;

        final Future<List<MerkleNode>> future = exec.submit(call);

        try {
            List<MerkleNode> result = future.get(timeout, unit);

            if (result.size() > 0) {
                exists = true;
            }
        } catch (TimeoutException e) {
            // full pass
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }

        return exists;
    }

    @PerformanceLog
    public boolean fileExists(String base58Hash, int timeout, TimeUnit unit) throws IOException {
        return this.fileExists(Multihash.fromBase58(base58Hash), timeout, unit);
    }

    @PerformanceLog
    public boolean fileExists(String base58Hash) throws IOException {
        return this.fileExists(base58Hash, this.defaultCheckExistsTimeoutSecs, TimeUnit.SECONDS);
    }

    @PerformanceLog
    public byte[] cat(String base58Hash, int timeout, TimeUnit unit) throws TimeoutException, IOException {
        Multihash hash = Multihash.fromBase58(base58Hash);

        final Callable<byte[]> call = () -> {
            byte[] data = null;

            data = this.ipfs.cat(hash);

            return data;
        };

        byte[] data = null;

        ExecutorService exec = Executors.newSingleThreadExecutor();
        final Future<byte[]> future = exec.submit(call);

        try {
            data = future.get(timeout, unit);
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }

        return data;
    }

    @PerformanceLog
    public byte[] cat(String base58Hash) throws TimeoutException, IOException {
        return this.cat(base58Hash, defaultDownloadTimeoutSecs, TimeUnit.SECONDS);
    }

    public Map<String, Object> stat(String base58Hash) throws IOException {
        return ipfs.object.stat(Multihash.fromBase58(base58Hash));
    }

    public InputStream catStream(String base58Hash) throws  TimeoutException, IOException {
        Multihash hash = Multihash.fromBase58(base58Hash);

        return ipfs.catStream(hash);
    }
}
