package com.songtaegeun.concurrencylab.common.api

import com.songtaegeun.concurrencylab.common.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(exception: ResourceNotFoundException): ErrorResponse =
        ErrorResponse(message = requireNotNull(exception.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(exception: MethodArgumentNotValidException): ErrorResponse =
        ErrorResponse(
            message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
                ?: "요청값이 올바르지 않습니다.",
        )
}
