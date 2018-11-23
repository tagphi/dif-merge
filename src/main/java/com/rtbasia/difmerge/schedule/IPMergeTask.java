package com.rtbasia.difmerge.schedule;

import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.ipfs.IPFSFileIterator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@Component
@Scope("prototype")
public class IPMergeTask extends MergeTask {
    public IPMergeTask(Job job) {
        super(job);
    }

    @Override
    public Map<String, Set<String>> merge() {
        Map<String, Set<String>> mergeResult =  super.merge();

        // 剔除媒体IP
        Map<String, Object> extraArgs = getArgs();

        List<String> publisherIpHash = (List<String>)extraArgs.get("publisherip");

        if (publisherIpHash != null && publisherIpHash.size() > 0) {
            try {
                new IPFSFileIterator(publisherIpHash, localIpfs).forEachLine((line, hash) -> {
                    mergeResult.remove(line);

                    return mergeResult;
                }, (i, total) -> {
                    String step = String.format("移除媒体IP(%d/%d)", i, publisherIpHash.size());
                    progress(step,"运行中","");
                });
            } catch (TimeoutException | IOException e) {
                logger.error("failed to download file", e);

                progress("下载媒体IP","失败", e.getMessage());
                throw new IllegalStateException(e);
            }
        }

        return mergeResult;
    }

    @Override
    public int getQuorum() {
        return 2;
    }
}
