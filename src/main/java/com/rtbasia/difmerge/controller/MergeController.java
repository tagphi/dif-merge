package com.rtbasia.difmerge.controller;

import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.mapper.JobMapper;
import com.rtbasia.difmerge.schedule.Scheduler;
import com.rtbasia.difmerge.validator.FileFormatException;
import com.rtbasia.difmerge.validator.DeltaFileValidator;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("type") String type,
                       @RequestParam("extraArgs") String extraArgs,
                       @RequestParam("callbackUrl") String callbackUrl,
                       @RequestParam("callbackArgs") String callbackArgs) throws IOException, FileFormatException {
        String tempFilePath = backupFile(file.getInputStream());

        // 校验文件格式
        DeltaFileValidator.validate(type, Paths.get(tempFilePath));

        Job job = new Job(tempFilePath, type, extraArgs, callbackUrl, callbackArgs);

        int id = jobMapper.addJob(job);
        job.setId(id);

        // 提交job到任务队列
        scheduler.addTask(job);
    }

    @PostMapping("/merge")
    public void merge(@RequestParam("type") String type,
                      @RequestParam("extraArgs") String extraArgs,
                      @RequestParam("callbackUrl") String callbackUrl,
                      @RequestParam("callbackArgs") String callbackArgs) {
        Job job = new Job("", type, extraArgs, callbackUrl, callbackArgs);

        int id = jobMapper.addJob(job);
        job.setId(id);

        // 提交job到任务队列
        scheduler.addTask(job);
    }
}
