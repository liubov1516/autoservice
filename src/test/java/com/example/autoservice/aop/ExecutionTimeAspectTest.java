package com.example.autoservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Тест аспекту (Around advice) НАПРЯМУ: ProceedingJoinPoint замокано вручну,
// Spring-контекст не запускається взагалі.
class ExecutionTimeAspectTest {

    private final ExecutionTimeRecorder recorder = new ExecutionTimeRecorder();
    private final ExecutionTimeAspect aspect = new ExecutionTimeAspect(recorder);

    @Test
    void measureCallsProceedAndReturnsItsResultAndRecordsDuration() throws Throwable {
        // Arrange
        ProceedingJoinPoint joinPoint = sampleJoinPoint();
        when(joinPoint.proceed()).thenReturn("ok");
        TrackExecutionTime annotation = annotationFrom(SampleTarget.class, "fast");

        // Act
        Object result = aspect.measure(joinPoint, annotation);

        // Assert
        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
        assertThat(recorder.callCount()).isEqualTo(1);
    }

    @Test
    void measurePropagatesExceptionFromProceed() throws Throwable {
        // Arrange
        ProceedingJoinPoint joinPoint = sampleJoinPoint();
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("бум"));
        TrackExecutionTime annotation = annotationFrom(SampleTarget.class, "fast");

        // Act + Assert
        assertThatThrownBy(() -> aspect.measure(joinPoint, annotation))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("бум");

        verify(joinPoint).proceed();
    }

    private ProceedingJoinPoint sampleJoinPoint() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(signature.toShortString()).thenReturn("RepairOrderService.findAllTracked()");
        when(joinPoint.getSignature()).thenReturn(signature);
        return joinPoint;
    }

    private TrackExecutionTime annotationFrom(Class<?> type, String methodName) throws NoSuchMethodException {
        return type.getDeclaredMethod(methodName).getAnnotation(TrackExecutionTime.class);
    }

    // Допоміжний клас лише для отримання реального екземпляра анотації через рефлексію
    static class SampleTarget {
        @TrackExecutionTime(warnThresholdMs = 50)
        public void fast() {
        }
    }
}
