package com.pontificia.remashorario.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String message;
    private T data;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object error;

    /** * Constructor para respuestas sin detalle técnico de error */
    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
        this.error = null;
    }

    /** * Respuesta exitosa con datos */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(null, data);
    }

    /** * Respuesta exitosa con datos y mensaje */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>( message, data);
    }

    /** * Respuesta de error con mensaje para el usuario */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>( message, null);
    }

    /** * Respuesta de error con mensaje para el usuario y detalle técnico */
    public static <T> ApiResponse<T> error(String message, Object errorDetail) {
        return new ApiResponse<>(message, null, errorDetail);
    }
}

