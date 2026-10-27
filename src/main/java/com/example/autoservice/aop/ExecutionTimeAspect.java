package com.example.autoservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Around advice: вимірює, скільки реально виконувався метод, і пише в лог/реєстр.
// Це реальна наскрізна функціональність - моніторинг продуктивності,
// а не копія прикладу логування з лекції.
@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    private final ExecutionTimeRecorder recorder;

    public ExecutionTimeAspect(ExecutionTimeRecorder recorder) {
        this.recorder = recorder;
    }

    @Around("@annotation(trackExecutionTime)")
    public Object measure(ProceedingJoinPoint joinPoint, TrackExecutionTime trackExecutionTime) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            recorder.record(duration);
            if (duration > trackExecutionTime.warnThresholdMs()) {
                log.warn("Метод {} виконувався {} мс (поріг {} мс)",
                        joinPoint.getSignature().toShortString(), duration, trackExecutionTime.warnThresholdMs());
            } else {
                log.info("Метод {} виконався за {} мс", joinPoint.getSignature().toShortString(), duration);
            }
        }
    }
}
