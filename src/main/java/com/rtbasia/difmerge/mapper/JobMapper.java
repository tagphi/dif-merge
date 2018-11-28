package com.rtbasia.difmerge.mapper;

import com.rtbasia.difmerge.entity.Job;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface JobMapper {
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into jobs (tempFilePath,`action`,extraArgs,callbackUrl,callbackArgs,version) " +
            "values(#{tempFilePath},#{action},#{extraArgs},#{callbackUrl},#{callbackArgs},#{version})")
    public Integer addJob(Job job);

    @Update("update jobs set step=#{step}, status=#{status}, message=#{message}, modifiedTime=NOW()" +
            "where id=#{id}")
    public Integer updateJob(Job job);

    @Select("select count(*) from jobs")
    public Integer getTotal();

    @Select("select * from jobs order by modifiedTime desc limit #{start}, #{end}")
    public List<Job> listJobs(@Param("start")int start, @Param("end") int end);

    @Select("select count(*) from jobs where `action`=#{action} and version=#{version} and status!='失败'")
    public int mergeJobExists(@Param("action")String action, @Param("version")int version);
}
