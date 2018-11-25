package com.rtbasia.difmerge.schedule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    final static ObjectMapper mapper = new ObjectMapper();

    protected Job job;

    public GenericJobTask(Job job) {
        this.job = job;
    }

    @Autowired
    RestTemplateBuilder builder;

    public abstract void doRun();

    @Override
    public void run() {
        doRun();

        try {
            job.cleanTempFile();
        } catch (IOException e) {
            logger.error("failed to delete temp file", e);
        }

        callback(job);
    }

    public void callback(Job job) {
        RestTemplate template =  builder.build();
        template.getMessageConverters().add(new FlexibleMappingJackson2HttpMessageConverter());

        Map<String, Object> callbackArgsMap = null;

        String argsJsonStr = job.getCallbackArgs();

        if (!StringUtils.isEmpty(argsJsonStr)) {
            beginStep("解析回调参数");

            try {
                callbackArgsMap = mapper.readValue(argsJsonStr, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                logger.error("failed to deserialize arg", e);
                onError(e.getMessage());

                throw new IllegalArgumentException(e);
            }

            endStep();
        } else {
            callbackArgsMap = new HashMap<>();
        }

        Map<String, Object> extCallBackArgs = job.getExtCallbackArgs();

        for (String key : extCallBackArgs.keySet()) {
            callbackArgsMap.put(key, extCallBackArgs.get(key));
        }

        logger.info("callback, update ledger ...");
        beginStep("写入账本");

        try {
            Map response = template.postForEntity(job.getCallbackUrl(), callbackArgsMap, Map.class).getBody();
        } catch (RestClientException e) {
            onError(e.getMessage());

            throw e;
        }

        endStep();
    }
}
