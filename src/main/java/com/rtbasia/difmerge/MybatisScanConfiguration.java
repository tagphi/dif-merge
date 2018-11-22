package com.rtbasia.difmerge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.rtbasia.difmerge.mapper")
public class MybatisScanConfiguration {
}
