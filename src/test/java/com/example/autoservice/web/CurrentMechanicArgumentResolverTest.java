package com.example.autoservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Тести для резолвера аргументів (Тип 3). supportsParameter перевіряємо без моків
// (звичайна рефлексія), resolveArgument - з моком NativeWebRequest.
class CurrentMechanicArgumentResolverTest {

    private final CurrentMechanicArgumentResolver resolver = new CurrentMechanicArgumentResolver();

    // ---------- AC6 ----------

    @Test
    void supportsParameterWithCurrentMechanicAnnotationReturnsTrue() throws NoSuchMethodException {
        // Arrange
        MethodParameter parameter = annotatedParameter();

        // Act
        boolean result = resolver.supportsParameter(parameter);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void supportsParameterWithoutAnnotationReturnsFalse() throws NoSuchMethodException {
        // Arrange
        MethodParameter parameter = plainParameter();

        // Act
        boolean result = resolver.supportsParameter(parameter);

        // Assert
        assertThat(result).isFalse();
    }

    // ---------- AC7 ----------

    @Test
    void resolveArgumentHeaderPresentReturnsHeaderValue() {
        // Arrange
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getHeader("X-Mechanic")).thenReturn("Олег Коваль");

        // Act
        Object result = resolver.resolveArgument(null, null, request, null);

        // Assert
        assertThat(result).isEqualTo("Олег Коваль");
    }

    @Test
    void resolveArgumentNoHeaderReturnsDefaultValue() {
        // Arrange
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getHeader("X-Mechanic")).thenReturn(null);

        // Act
        Object result = resolver.resolveArgument(null, null, request, null);

        // Assert
        assertThat(result).isEqualTo("unknown");
    }

    // ---------- допоміжні методи ----------

    private MethodParameter annotatedParameter() throws NoSuchMethodException {
        return new MethodParameter(SampleController.class.getMethod("withAnnotation", String.class), 0);
    }

    private MethodParameter plainParameter() throws NoSuchMethodException {
        return new MethodParameter(SampleController.class.getMethod("withoutAnnotation", String.class), 0);
    }

    // Допоміжний клас лише для рефлексії над параметрами методів
    static class SampleController {
        public void withAnnotation(@CurrentMechanic String mechanic) {
        }

        public void withoutAnnotation(String mechanic) {
        }
    }
}
