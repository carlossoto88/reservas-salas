package com.ejemplo.reservas.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.TreeMap;

/**
 * Convierte las excepciones en respuestas JSON con el código HTTP adecuado.
 * Así los controladores no tienen que preocuparse de los errores.
 */
@RestControllerAdvice
public class ManejadorErrores {

    // 400: fallan las anotaciones de validación (@NotBlank, @Min, ...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new TreeMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return new ErrorResponse(400, "Hay datos no válidos", errores);
    }

    // 400: reglas que comprobamos a mano en el servicio (p. ej. fin antes que inicio)
    @ExceptionHandler(DatosInvalidosException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse datosInvalidos(DatosInvalidosException ex) {
        return new ErrorResponse(400, "Hay datos no válidos", Map.of(ex.getCampo(), ex.getMessage()));
    }

    // 400: el JSON está mal escrito o una fecha no tiene el formato correcto
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse jsonMalFormado(HttpMessageNotReadableException ex) {
        return new ErrorResponse(400,
                "El JSON no es válido o algún campo tiene un formato incorrecto. "
                        + "Las fechas van así: 2030-01-15T10:00:00");
    }

    // 400: un parámetro de la URL tiene un formato incorrecto (p. ej. fecha=hola)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse parametroIncorrecto(MethodArgumentTypeMismatchException ex) {
        return new ErrorResponse(400, "Hay datos no válidos",
                Map.of(ex.getName(), "El valor '" + ex.getValue() + "' no tiene un formato válido"));
    }

    // 404
    @ExceptionHandler(NoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noEncontrado(NoEncontradoException ex) {
        return new ErrorResponse(404, ex.getMessage());
    }

    // 409
    @ExceptionHandler(ConflictoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse conflicto(ConflictoException ex) {
        return new ErrorResponse(409, ex.getMessage());
    }
}
