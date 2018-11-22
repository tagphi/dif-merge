package com.rtbasia.difmerge.schedule;

import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.mapper.JobMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class Scheduler implements ApplicationContextAware {
    final static Logger logger = LoggerFactory.getLogger(DeltaUploadTask.class);

    private Executor executor = Executors.newSingleThreadExecutor();
    private ApplicationContext ctx;

    @Autowired
    private JobMapper jobMapper;

    private AbstractTask buildTask(Job job) {
        String action = job.getAction();

        if ("deltaupload".equals(job.getAction())) {
            return ctx.getBean(DeltaUploadTask.class, job);
        } else if ("mergeDevice".equals(job.getAction())) {
            return ctx.getBean(DeviceMergeTask.class, job);
        } else {
            throw new IllegalArgumentException("unkown action " + action);
        }
    }

    public void addTask(Job job) {
        AbstractTask task = buildTask(job); // TODO: 根据任务不同分配不同的task

        task.addProgressLisener(new ProgressLisener() {
            @Override
            public void onProgress(String step, String status, String message) {
                logger.info(String.format("Step: %s, Status: %s, Message: %s", step, status, message));

                job.setMessage(message);
                job.setStep(step);
                job.setStatus(status);

                jobMapper.updateJob(job);
            }
        });

        executor.execute(task);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
