package com.rtbasia.difmerge.schedule;

import com.rtbasia.difmerge.entity.Job;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DeviceMergeTask extends MergeTask {

    public DeviceMergeTask(Job job) {
        super(job);
    }

    @Override
    public int getQuorum() {
        return 1;
    }
}
