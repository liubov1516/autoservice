package com.example.autoservice.aop;

import com.example.autoservice.mapper.RepairOrderMapper;
import com.example.autoservice.repository.RepairOrderRepository;
import com.example.autoservice.service.RepairOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Обов'язкова частина 2 лабораторної №4: демонстрація проблеми self-invocation.
// Тут НЕ мокається ProceedingJoinPoint (той тест окремо, у ExecutionTimeAspectTest) -
// тут потрібен реальний Spring AOP-проксі, тому піднято маленький Spring-контекст
// лише з потрібними бінами (без веб-шару, без Security, без мережі).
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SelfInvocationDemoTest.TestConfig.class)
class SelfInvocationDemoTest {

    @Autowired
    private RepairOrderService service;

    @Autowired
    private ExecutionTimeRecorder recorder;

    @Test
    void externalCallThroughProxyTriggersAspect() {
        // Act: виклик ЗЗОВНІ, через біна (проксі) - так, як покликав би контролер або тест
        service.findAllTracked();

        // Assert: аспект спрацював і записав тривалість
        assertThat(recorder.callCount()).isEqualTo(1);
    }

    @Test
    void selfInvocationBypassesProxyAndAspectDoesNotFire() {
        // Act: findAllViaSelfInvocation() усередині себе викликає this.findAllTracked() -
        // такий виклик іде напряму на об'єкт, минаючи AOP-проксі
        service.findAllViaSelfInvocation();

        // Assert: аспект НЕ спрацював - це свідомий задум вправи, а не баг
        assertThat(recorder.callCount()).isZero();
    }

    @EnableAspectJAutoProxy
    @Configuration
    static class TestConfig {

        @Bean
        RepairOrderRepository repairOrderRepository() {
            RepairOrderRepository repository = mock(RepairOrderRepository.class);
            when(repository.findAll()).thenReturn(List.of());
            when(repository.findById(any())).thenReturn(Optional.empty());
            return repository;
        }

        @Bean
        RepairOrderMapper repairOrderMapper() {
            return mock(RepairOrderMapper.class);
        }

        @Bean
        RepairOrderService repairOrderService(RepairOrderRepository repository, RepairOrderMapper mapper) {
            return new RepairOrderService(repository, mapper);
        }

        @Bean
        ExecutionTimeRecorder executionTimeRecorder() {
            return new ExecutionTimeRecorder();
        }

        @Bean
        ExecutionTimeAspect executionTimeAspect(ExecutionTimeRecorder recorder) {
            return new ExecutionTimeAspect(recorder);
        }
    }
}
