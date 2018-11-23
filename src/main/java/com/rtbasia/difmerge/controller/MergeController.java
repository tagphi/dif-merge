package com.rtbasia.difmerge.controller;

import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.http.ListJobResponse;
import com.rtbasia.difmerge.http.SubmitResponse;
import com.rtbasia.difmerge.mapper.JobMapper;
import com.rtbasia.difmerge.schedule.Scheduler;
import com.rtbasia.difmerge.validator.AppealValidator;
import com.rtbasia.difmerge.validator.FileFormatException;
import com.rtbasia.difmerge.validator.DeltaFileValidator;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
public class MergeController {
    @Value("${spring.application.tempFileDir}")
    private String tempFileDir;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private Scheduler scheduler;

    /**
     * 拷贝待合并文件到临时目录
     * @param is
     */
    private String backupFile(InputStream is) throws IOException {
        String uuid = UUID.randomUUID().toString();

        Path dirPath = Paths.get(tempFileDir);

        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath); // make sure temp dir exists
        }

        Path filePath = dirPath.resolve(uuid);

        IOUtils.copy(is, Files.newOutputStream(filePath));

        return filePath.toAbsolutePath().toString();
    }

    @PostMapping("/deltaUpload")
    public SubmitResponse deltaUpload(@RequestParam("file") MultipartFile file,
                                      @RequestParam("type") String type,
                                      @RequestParam("extraArgs") String extraArgs,
                                      @RequestParam("callbackUrl") String callbackUrl,
                                      @RequestParam("callbackArgs") String callbackArgs)
            throws IOException, FileFormatException {
        String tempFilePath = backupFile(file.getInputStream());

        // 校验文件格式
        DeltaFileValidator.validate(type, Paths.get(tempFilePath));

        Job job = new Job(tempFilePath, "deltaUpload" + type, extraArgs, callbackUrl, callbackArgs);

        jobMapper.addJob(job);

        // 提交job到任务队列
        scheduler.addTask(job);

        return SubmitResponse.ok();
    }

    @PostMapping("/merge")
    public SubmitResponse merge(@RequestParam("type") String type,
                      @RequestParam("extraArgs") String extraArgs,
                      @RequestParam("callbackUrl") String callbackUrl,
                      @RequestParam("callbackArgs") String callbackArgs) {
        Job job = new Job("", "merge" + type, extraArgs, callbackUrl, callbackArgs);

        jobMapper.addJob(job);

        // 提交job到任务队列
        scheduler.addTask(job);

        return SubmitResponse.ok();
    }

    @PostMapping("/appeal")
    public SubmitResponse appeal(@RequestParam("file") MultipartFile file,
                                 @RequestParam("type") String type,
                                 @RequestParam("callbackUrl") String callbackUrl,
                                 @RequestParam("callbackArgs") String callbackArgs)
            throws IOException, FileFormatException {
        String tempFilePath = backupFile(file.getInputStream());

        // 校验文件格式
        AppealValidator.validate(type, Paths.get(tempFilePath));

        Job job = new Job(tempFilePath, "appeal" + type, null, callbackUrl, callbackArgs);

        jobMapper.addJob(job);

        // 提交job到任务队列
        scheduler.addTask(job);

        return SubmitResponse.ok();
    }

    @GetMapping("/jobs")
    public ListJobResponse listJob(@RequestParam("start") int start,
                                   @RequestParam("end") int end) {
        int total = jobMapper.getTotal();

        List<Job> jobs = jobMapper.listJobs(start, end);

        ListJobResponse response = new ListJobResponse();

        response.setTotal(total);
        response.setJobs(jobs);

        return response;
    }
}
