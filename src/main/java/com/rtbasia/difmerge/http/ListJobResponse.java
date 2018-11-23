package com.rtbasia.difmerge.http;

import com.rtbasia.difmerge.entity.Job;

import java.util.List;

public class ListJobResponse {
    private int total;
    private List<Job> jobs;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
