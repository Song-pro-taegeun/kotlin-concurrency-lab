package com.songtaegeun.concurrencylab.product.api

import com.songtaegeun.concurrencylab.product.application.ProductService
import com.songtaegeun.concurrencylab.product.dto.CreateProductRequest
import com.songtaegeun.concurrencylab.product.dto.CreateProductResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) {
    @Operation(
        summary = "상품 등록",
        description = "상품과 초기 재고, 배송 예정 정보를 함께 등록하는 API",
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: CreateProductRequest,
    ): CreateProductResponse = productService.create(request)
}
