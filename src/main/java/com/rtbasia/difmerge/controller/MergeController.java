package com.rtbasia.difmerge.controller;

import com.rtbasia.difmerge.entity.Job;
import com.rtbasia.difmerge.http.ListJobResponse;
import com.rtbasia.difmerge.http.SubmitResponse;
import com.rtbasia.difmerge.ipfs.IPFSClient;
import com.rtbasia.difmerge.mapper.JobMapper;
import com.rtbasia.difmerge.schedule.AppealTask;
import com.rtbasia.difmerge.schedule.Scheduler;
import com.rtbasia.difmerge.validator.AppealValidator;
import com.rtbasia.difmerge.validator.FileFormatException;
import com.rtbasia.difmerge.validator.DeltaFileValidator;
import com.rtbasia.difmerge.validator.IpAppealValidator;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@RestController
public class MergeController {
    final static Logger logger = LoggerFactory.getLogger(MergeController.class);

    @Value("${spring.application.tempFileDir}")
    private String tempFileDir;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    @Qualifier(value="localIpfs")
    private IPFSClient localIpfs;

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

    @PostMapping("/publisherIp")
    public SubmitResponse uploadPublisherIp(@RequestParam("file") MultipartFile file,
                                            @RequestParam("callbackUrl") String callbackUrl,
                                            @RequestParam("callbackArgs") String callbackArgs)
            throws IOException, FileFormatException {
        String tempFilePath = backupFile(file.getInputStream());

        // 校验文件格式
        new IpAppealValidator(Paths.get(tempFilePath)).validate();

        Job job = new Job(tempFilePath, "uploadPublisherIp", null, callbackUrl, callbackArgs);

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

    @RequestMapping(value = "/download/{hash}", method = RequestMethod.GET)
    public void getFile(
            @PathVariable("hash") String hash,
            HttpServletResponse response) {
        try {
            response.setContentType("application/octet-stream");
            response.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

            boolean exists = localIpfs.fileExists(hash);

            if (!exists) {
                response.sendError(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
                return;
            }

            InputStream is = localIpfs.catStream(hash);
            IOUtils.copy(is, response.getOutputStream());

            response.flushBuffer();
        } catch (IOException | TimeoutException e) {
            logger.info("error writing file to output stream, hash '{}'", hash, e);

            throw new RuntimeException("IOError writing file to output stream", e);
        }
    }
}
