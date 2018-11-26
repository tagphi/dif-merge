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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
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

    private Set<String> filterByFlag(Stream<String> stream, String flag) {
        return stream.map(l -> l.split("\t"))
                .filter(c -> flag.equals(c[c.length - 1]))
                .map(c -> String.join("\t", Arrays.asList(c).subList(0, c.length - 1)))
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    @PerformanceLog
    public void doRun() {
        String argsJsonStr = job.getExtraArgs();
        Map<String, Object> argsMap = null;

        try {
            beginStep("解析参数");
            argsMap = mapper.readValue(argsJsonStr, new TypeReference<Map<String, Object>>() {
            });
            endStep();

            String oldHash = (String) argsMap.get("oldHash");
            Set<String> oldList = null;

            if (oldHash != null) {
                // 1. 下载旧列表
                beginStep("下载旧列表");
                logger.info("download old file: " + oldHash);

                if (!localIpfs.fileExists(oldHash)) {
                    throw new FileNotFoundException(oldHash);
                }

                oldList = new HashSet(Arrays.asList(new String(localIpfs.cat(oldHash)).split("\n")));

                endStep();
            }

            // 2. 进行合并
            logger.info("merging ...");
            beginStep("合并");

            HashSet<String> deltaList = null;

            Supplier<Stream<String>> supplier = () -> {
                try {
                    return Files.lines(Paths.get(job.getTempFilePath()));
                } catch (IOException e) {
                    logger.error("failed to merge delta list to old list", e);
                    onError(e.getMessage());

                    throw new IllegalStateException(e);
                }
            };

            // 取得标识位为0的记录，为待删除
            Set<String> removeItems = filterByFlag(supplier.get(), "0");
            logger.info(String.format("delete %d records \n", removeItems.size()));

            // 取得标志位为1的记录，为增加
            Set<String> newItems = filterByFlag(supplier.get(), "1");
            logger.info(String.format("add %d records \n", newItems.size()));
            String newList = null;

            if (removeItems.size() > 0 || newItems.size() > 0) {
                newList = merge(oldList, newItems, removeItems);
            } else {
                logger.warn("delta list is empty");
                return;
            }

            endStep();

            // 3. 上传新文件到ipfs
            logger.info("upload new file to ipfs ...");
            beginStep("上传新列表");
            String newHash = remoteIpfs.upload(newList).hash.toBase58();
            logger.info("upload new file complete, hash: " + newHash);
            endStep();

            // 4. 上传增量到ipfs
            logger.info("upload new delta file to ipfs ...");
            beginStep("上传增量列表");
            String deltaHash = remoteIpfs.uploadFile(job.getTempFilePath()).hash.toBase58();
            logger.info("upload delta file complete, hash: " + deltaHash);
            endStep();

            job.putExtCallbackArgs("fullHash", newHash);
            job.putExtCallbackArgs("deltaHash", deltaHash);
        } catch (IOException | TimeoutException e) {
            onError(e.getMessage());

            throw new IllegalStateException(e);
        }
    }

    public String merge(Set<String> oldList, Set<String> addItems, Set<String> removeItems) {
        Set<String> newList = oldList != null ? oldList.stream()
                .filter(i -> removeItems.contains(i)).collect(Collectors.toSet()) : new HashSet<>();
        newList.addAll(addItems);

        return newList.stream().collect(Collectors.joining("\n"));
    }
}
