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

        if (action.startsWith("deltaUpload")) {
            return ctx.getBean(DeltaUploadTask.class, job);
        } else if ("mergeDevice".equals(action)) {
            return ctx.getBean(DeviceMergeTask.class, job);
        } else if ("mergeIP".equals(action)) {
            return ctx.getBean(IPMergeTask.class, job);
        } else if ("mergeDefaultDevice".equals(action)) {
            return ctx.getBean(DefaultDeviceMergeTask.class, job);
        } else if (action.startsWith("appeal")) {
            return ctx.getBean(AppealTask.class, job);
        } else if ("uploadPublisherIp".equals(action)) {
            return ctx.getBean(PublisherIpTask.class, job);
        } else {
            throw new IllegalArgumentException("unkown action " + action);
        }
    }

    public void addTask(Job job) {
        AbstractTask task = buildTask(job);

        task.addProgressLisener(new ProgressLisener() {
            @Override
            public void onStart(String step) {
                job.setMessage("");
                job.setStep(step);
                job.setStatus("运行中");

                logger.info(String.format("Step: %s, Status: %s, Message: %s",
                        job.getStep(), job.getStatus(), job.getMessage()));

                jobMapper.updateJob(job);
            }

            @Override
            public void onError(String message) {
                job.setMessage(message);
                job.setStatus("失败");

                logger.error(String.format("Step: %s, Status: %s, Message: %s",
                        job.getStep(), job.getStatus(), job.getMessage()));

                jobMapper.updateJob(job);
            }

            @Override
            public void onComplete() {
                job.setStatus("成功");

                logger.info(String.format("Step: %s, Status: %s, Message: %s",
                        job.getStep(), job.getStatus(), job.getMessage()));

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
