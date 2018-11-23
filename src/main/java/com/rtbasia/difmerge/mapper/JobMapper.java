package com.rtbasia.difmerge.mapper;

import com.rtbasia.difmerge.entity.Job;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Select("select * from jobs limit #{arg0}, #{arg1}")
    public List<Job> listJobs(int start, int end);
}
