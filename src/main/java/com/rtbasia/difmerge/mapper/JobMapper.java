package com.rtbasia.difmerge.mapper;

import com.rtbasia.difmerge.entity.Job;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface JobMapper {
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into jobs (tempFilePath,`action`,extraArgs,callbackUrl,callbackArgs) " +
            "values(#{tempFilePath},#{action},#{extraArgs},#{callbackUrl},#{callbackArgs})")
    public Integer addJob(Job job);

    @Update("update jobs set step=#{step}, status=#{status}, message=#{message}, modifiedTime=NOW()" +
            "where id=#{id}")
    public Integer updateJob(Job job);

    @Select("select count(*) from jobs")
    public Integer getTotal();

    @Select("select * from jobs order by modifiedTime desc limit #{start}, #{end}")
    public List<Job> listJobs(@Param("start")int start, @Param("end") int end);
}
