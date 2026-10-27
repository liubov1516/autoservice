package com.example.autoservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentMechanicArgumentResolverTest {

    private final CurrentMechanicArgumentResolver resolver = new CurrentMechanicArgumentResolver();

    @Test
    void supportsParameterWithCurrentMechanicAnnotationReturnsTrue() throws NoSuchMethodException {
        MethodParameter parameter = annotatedParameter();

        boolean result = resolver.supportsParameter(parameter);

        assertThat(result).isTrue();
    }

    @Test
    void supportsParameterWithoutAnnotationReturnsFalse() throws NoSuchMethodException {
        MethodParameter parameter = plainParameter();

        boolean result = resolver.supportsParameter(parameter);

        assertThat(result).isFalse();
    }

    @Test
    void resolveArgumentHeaderPresentReturnsHeaderValue() {
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getHeader("X-Mechanic")).thenReturn("Олег Коваль");

        Object result = resolver.resolveArgument(null, null, request, null);

        assertThat(result).isEqualTo("Олег Коваль");
    }

    @Test
    void resolveArgumentNoHeaderReturnsDefaultValue() {
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getHeader("X-Mechanic")).thenReturn(null);

        Object result = resolver.resolveArgument(null, null, request, null);

        assertThat(result).isEqualTo("unknown");
    }

    private MethodParameter annotatedParameter() throws NoSuchMethodException {
        return new MethodParameter(SampleController.class.getMethod("withAnnotation", String.class), 0);
    }

    private MethodParameter plainParameter() throws NoSuchMethodException {
        return new MethodParameter(SampleController.class.getMethod("withoutAnnotation", String.class), 0);
    }

    static class SampleController {
        public void withAnnotation(@CurrentMechanic String mechanic) {
        }

        public void withoutAnnotation(String mechanic) {
        }
    }
}
