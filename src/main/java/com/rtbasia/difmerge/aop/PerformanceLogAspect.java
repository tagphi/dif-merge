package com.rtbasia.difmerge.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceLogAspect {
    final static Logger logger = LoggerFactory.getLogger(PerformanceLogAspect.class);

    @Pointcut("@annotation(com.rtbasia.difmerge.aop.PerformanceLog)")
    public void pointcut() { }

    private long bytestoMb(long sizeInBytes) {
        return sizeInBytes / 1024 /1024;
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable  {
        long startTime = System.currentTimeMillis();

        Runtime runtime = Runtime.getRuntime();

        long totalMem = runtime.totalMemory();
        long usedMemBefore = bytestoMb(totalMem - runtime.freeMemory());

        Object result  = point.proceed();

        long timeCost = System.currentTimeMillis() - startTime;

        long usedMemAfter = bytestoMb(totalMem - runtime.freeMemory());

        MethodSignature signature = (MethodSignature) point.getSignature();
        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();

        logger.info(String.format("%s.%s(): time cost: %d ms, total mem: %d MB, before used: %d MB, after used: %d MB",
                className, methodName, timeCost, bytestoMb(totalMem), usedMemBefore, usedMemAfter));

        return result;
    }
}
