package com.rtbasia.difmerge.mapper;

import com.rtbasia.difmerge.entity.Job;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface JobMapper {
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into jobs (tempFilePath,`action`,extraArgs,callbackUrl,callbackArgs) " +
            "values(#{tempFilePath},#{action},#{extraArgs},#{callbackUrl},#{callbackArgs})")
    public Integer addJob(Job job);

    @Insert("update jobs set step=#{action}, status=#{status}, message=#{message} " +
            "where id=#{id}")
    public Integer updateJob(Job job);
}
