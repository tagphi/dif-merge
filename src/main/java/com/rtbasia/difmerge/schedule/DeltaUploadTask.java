package com.rtbasia.difmerge.schedule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtbasia.difmerge.aop.PerformanceLog;
import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.ipfs.IPFSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Scope("prototype")
public class DeltaUploadTask extends GenericJobTask {
    final static Logger logger = LoggerFactory.getLogger(DeltaUploadTask.class);
    final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    @Qualifier(value="localIpfs")
    private IPFSClient localIpfs;

    @Autowired
    @Qualifier(value="remoteIpfs")
    private IPFSClient remoteIpfs;

    public DeltaUploadTask(Job job) {
        super(job);
    }

    @Override
    @PerformanceLog
    public String doRun() {
        String argsJsonStr = job.getExtraArgs();
        Map<String, Object> argsMap = null;

        try {
            argsMap = mapper.readValue(argsJsonStr, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            logger.error("failed to deserialize arg", e);
            progress("解析参数", "失败", e.getMessage());

            throw new IllegalStateException(e);
        }

        String oldHash = (String)argsMap.get("oldHash");
        Set<String> oldList = null;

        if (oldHash != null) {
            // 1. 下载旧列表
            logger.info("download old file: " + oldHash);
            progress("下载旧列表", "运行中", "");

            try {
                oldList =  new HashSet(Arrays.asList(new String(localIpfs.cat(oldHash)).split("\n")));
            } catch (Exception e) {
                logger.error("failed to download old list", e);
                progress("下载旧列表", "失败", e.getMessage());

                throw new IllegalStateException(e);
            }
        }

        // 2. 进行合并
        logger.info("merging ...");
        progress("合并", "运行中", "");

        HashSet<String> deltaList = null;

        Supplier<Stream<String>> supplier = () -> {
            try {
                return Files.lines(Paths.get(job.getTempFilePath()));
            } catch (IOException e) {
                logger.error("failed to merge delta list to old list", e);
                progress("合并", "失败", e.getMessage());

                throw new IllegalStateException(e);
            }
        };

        // 取得标识位为0的记录，为待删除
        Set<String> removeItems = supplier.get().map(l -> l.split("\t"))
                .filter(c -> "0".equals(c[c.length - 1]))
                .map(c -> String.join("\t", Arrays.asList(c).subList(0, c.length - 2)))
                .distinct()
                .collect(Collectors.toSet());

        logger.info(String.format("delete %d records \n", removeItems.size()));

        // 取得标志位为1的记录，为增加
        Set<String> newItems = supplier.get().map(l -> l.split("\t"))
                .filter(c -> "1".equals(c[c.length - 1]))
                .map(c -> String.join("\t", Arrays.asList(c).subList(0, c.length - 2)))
                .distinct()
                .collect(Collectors.toSet());

        logger.info(String.format("add %d records \n", newItems.size()));

        String newList = null;

        if (removeItems.size() > 0 || newItems.size() > 0) {
            newList = merge(oldList, newItems, removeItems);
        } else {
            logger.warn("delta list is empty");
            return null;
        }

        // 3. 上传新文件到ipfs
        logger.info("upload new file to ipfs ...");
        progress("上传新列表", "运行中", "");

        String newHash = null;

        try {
            newHash = remoteIpfs.upload(newList).hash.toBase58();

            logger.info("upload new file complete, hash: " + newHash);
        } catch (IOException e) {
            logger.error("failed to upload new list to ipfs", e);
            progress("上传新列表", "失败", e.getMessage());

            throw new IllegalStateException(e);
        }

        // 4. 回调callback url
        return newHash;
    }

    public String merge(Set<String> oldList, Set<String> addItems, Set<String> removeItems) {
        Set<String> newList = oldList != null ? oldList.stream()
                .filter(i -> removeItems.contains(i)).collect(Collectors.toSet()) : new HashSet<>();
        newList.addAll(addItems);

        return newList.stream().collect(Collectors.joining("\n"));
    }
}
