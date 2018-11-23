package com.rtbasia.difmerge.schedule;

import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.http.FlexibleMappingJackson2HttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class GenericJobTask extends AbstractTask {
    final static Logger logger = LoggerFactory.getLogger(GenericJobTask.class);

    protected Job job;

    public GenericJobTask(Job job) {
        this.job = job;
    }

    @Autowired
    RestTemplateBuilder builder;

    public abstract String doRun();

    @Override
    public void run() {
        String hash = doRun();

        // 清理临时文件
        String tempFilePath = job.getTempFilePath();

        if (!StringUtils.isEmpty(tempFilePath)) {
            try {
                Files.deleteIfExists(Paths.get(tempFilePath));
            } catch (IOException e) {
                logger.error("failed to delete temp file " + tempFilePath, e);
            }
        }

        callback(hash, job);
    }

    public void callback(String hash, Job job) {
        RestTemplate template =  builder.build();
        template.getMessageConverters().add(new FlexibleMappingJackson2HttpMessageConverter());

        Map<String, String> data = new HashMap<>();

        data.put("hash", hash);
        data.put("callbackArgs", job.getCallbackArgs());

        logger.info("callback, update ledger ...");
        progress("写入账本", "运行中", "");

        Map response = null;

        try {
            response = template.postForEntity(job.getCallbackUrl(), data, Map.class).getBody();
        } catch (RestClientException e) {
            progress("写入账本", "失败", e.getMessage());

            throw e;
        }
    }
}
