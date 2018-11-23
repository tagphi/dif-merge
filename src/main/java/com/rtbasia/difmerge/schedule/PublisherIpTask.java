package com.rtbasia.difmerge.schedule;

import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.ipfs.IPFSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope("prototype")
public class PublisherIpTask extends GenericJobTask {
    final static Logger logger = LoggerFactory.getLogger(AppealTask.class);

    @Autowired
    @Qualifier(value="localIpfs")
    private IPFSClient localIpfs;

    @Autowired
    @Qualifier(value="remoteIpfs")
    private IPFSClient remoteIpfs;

    public PublisherIpTask(Job job) {
        super(job);
    }

    @Override
    public void doRun() {
        // 上传文件到ipfs
        logger.info("upload new file to ipfs ...");
        progress("上传媒体IP文件", "运行中", "");

        String newHash = null;

        try {
            newHash = remoteIpfs.uploadFile(job.getTempFilePath()).hash.toBase58();

            logger.info("upload new file complete, hash: " + newHash);
        } catch (IOException e) {
            logger.error("failed to upload new list to ipfs", e);
            progress("上传媒体IP文件", "失败", e.getMessage());

            throw new IllegalStateException(e);
        }

        job.putExtCallbackArgs("hash", newHash);

        return;
    }
}