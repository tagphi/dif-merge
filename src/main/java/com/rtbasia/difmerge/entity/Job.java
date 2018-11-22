package com.rtbasia.difmerge.entity;

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
}
