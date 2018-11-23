package com.rtbasia.difmerge.entity;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Job {
    private int id;
    private String tempFilePath;
    private String action;
    private String callbackUrl;
    private String extraArgs;
    private String callbackArgs;
    private String step;
    private String message;
    private String status;
    private Timestamp createTime;
    private Timestamp modifiedTime;
    private Map<String, Object> extCallbackArgs = new HashMap<>();

    public Job(int id,
               String tempFilePath,
               String action,
               String extraArgs,
               String callbackUrl,
               String callbackArgs,
               String step,
               String status,
               String message,
               Timestamp createTime,
               Timestamp modifiedTime) {
        this.tempFilePath = tempFilePath;
        this.action = action;
        this.extraArgs = extraArgs;
        this.callbackUrl = callbackUrl;
        this.callbackArgs = callbackArgs;
        this.step = step;
        this.message = message;
        this.status = status;
        this.createTime = createTime;
        this.modifiedTime = modifiedTime;
    }

    public Job(String tempFilePath,
               String action,
               String extraArgs,
               String callbackUrl,
               String callbackArgs) {
        this.tempFilePath = tempFilePath;
        this.action = action;
        this.extraArgs = extraArgs;
        this.callbackUrl = callbackUrl;
        this.callbackArgs = callbackArgs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTempFilePath() {
        return tempFilePath;
    }

    public void setTempFilePath(String tempFilePath) {
        this.tempFilePath = tempFilePath;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCallbackArgs() {
        return callbackArgs;
    }

    public void setCallbackArgs(String callbackArgs) {
        this.callbackArgs = callbackArgs;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExtraArgs() {
        return extraArgs;
    }

    public void setExtraArgs(String extraArgs) {
        this.extraArgs = extraArgs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Timestamp modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void cleanTempFile() throws IOException {
        // 清理临时文件
        String tempFilePath = this.getTempFilePath();

        if (!StringUtils.isEmpty(tempFilePath)) {
            Files.deleteIfExists(Paths.get(tempFilePath));
        }
    }

    public void putExtCallbackArgs(String key, Object value) {
        this.extCallbackArgs.put(key, value);
    }

    public Map<String, Object> getExtCallbackArgs() {
        return extCallbackArgs;
    }
}
