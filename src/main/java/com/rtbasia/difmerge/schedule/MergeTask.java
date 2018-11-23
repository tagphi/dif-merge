package com.rtbasia.difmerge.schedule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtbasia.difmerge.aop.PerformanceLog;
import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.ipfs.IPFSClient;
import com.rtbasia.difmerge.ipfs.IPFSFileIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

//TODO: 如果以后文件非常大，可能需要外排序和合并
public abstract class MergeTask extends GenericJobTask {
    final static Logger logger = LoggerFactory.getLogger(MergeTask.class);

    final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    @Qualifier(value="localIpfs")
    protected IPFSClient localIpfs;

    @Autowired
    @Qualifier(value="remoteIpfs")
    protected IPFSClient remoteIpfs;

    public MergeTask(Job job) {
        super(job);
    }

    public abstract int getQuorum();

    protected Map<String, Object> getArgs() {
        Map<String, Object> argsMap = null;

        try {
            String argsJsonStr = job.getExtraArgs();
            argsMap = mapper.readValue(argsJsonStr, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            logger.error("failed to deserialize arg", e);
            progress("解析参数", "失败", e.getMessage());

            throw new IllegalArgumentException(e);
        }

        return argsMap;
    }

    @PerformanceLog
    public Map<String, Set<String>> merge() {
        // 1. 下载每个公司的黑名单
        // 2. 按记录分组，统计每条记录的条数
        // 3. 根据类型过滤满足票数的结果，ip和默认设备为2票，设备为1票
        // 3. 下载移除列表数据
        // 4. 从第3步结果中删除移除列表数据
        // 5. 如果是ip类型，下载媒体ip，从第4步中删除媒体ip数据
        // 6. 上传最终结果到ipfs
        // 7. 回调，写入账本
        Map<String, Object> argsMap = getArgs();

        List<String> blacklistHash = (List<String>)argsMap.get("blacklist");

        Map<String, Set<String>> mergedResultVotes =  new ConcurrentHashMap<>();

        // 处理各公司黑名单
        if (blacklistHash != null && blacklistHash.size() > 0) {
            try {
                new IPFSFileIterator(blacklistHash, localIpfs).forEachLine((line, hash) -> {
                    if (mergedResultVotes.containsKey(line)) {
                        Set<String> hSet = mergedResultVotes.get(line);
                        hSet.add(hash);

                        mergedResultVotes.put(line, hSet);
                    } else {
                        Set<String> hSet = new HashSet<>();
                        hSet.add(hash);

                        mergedResultVotes.put(line, hSet);
                    }

                    return mergedResultVotes;
                }, (j, total) -> {
                    String step = String.format("合并黑名单(%d/%d)", j, blacklistHash.size());
                    progress(step,"运行中", "");
                });
            } catch (TimeoutException | IOException e) {
                logger.error("failed to download file", e);
                progress("合并黑名单失败","运行中", e.getMessage());
                throw new IllegalStateException(e);
            }

            int quorum =  getQuorum();

            for (String key : mergedResultVotes.keySet()) {
                if (mergedResultVotes.get(key).size() < quorum) {
                    mergedResultVotes.remove(key); // 删除不足票数的记录
                }
            }

            if (mergedResultVotes.size() == 0) {
                logger.warn("member's blacklist is empty, please check!");
            }
        }

        List<String> removelistsHash = (List<String>)argsMap.get("removelist");

        // 处理移除列表
        if (removelistsHash != null && removelistsHash.size() > 0) {
            try {
                new IPFSFileIterator(removelistsHash, localIpfs).forEachLine((line, hash) -> {
                    mergedResultVotes.remove(line);

                    return mergedResultVotes;
                }, (i, total) -> {
                    String step = String.format("处理移除(%d/%d)", i, removelistsHash.size());
                    progress(step,"运行中","");
                });
            } catch (TimeoutException | IOException e) {
                logger.error("failed to download file", e);

                progress("合并移除列表","失败", e.getMessage());
                throw new IllegalStateException(e);
            }
        }

        return mergedResultVotes;
    }

    @Override
    @PerformanceLog
    public String doRun() {
        Map<String, Set<String>> mergedResult = merge();
        String hash = null;

        if (mergedResult == null) {
            return "";
        }

        // 上传到ipfs
        try {
            hash = remoteIpfs.upload(mergedResult.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(c -> String.format("%s:%s", c.getKey(),
                            c.getValue().stream().sorted().collect(Collectors.joining(","))))
                    .collect(Collectors.joining("\n"))).hash.toBase58();
        } catch (IOException e) {
            logger.error("failed to upload to ipfs", e);
            progress("上传失败", "失败", e.getMessage());
        }

        logger.info("uploaded merged result to ipfs, hash: " + hash);

        return hash;
    }
}
