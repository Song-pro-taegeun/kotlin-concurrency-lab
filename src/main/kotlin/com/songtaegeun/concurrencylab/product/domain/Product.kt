package com.songtaegeun.concurrencylab.product.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

// 판매 상품의 기본 정보를 관리하는 엔티티
@Entity
@Table(name = "products")
class Product(
    // 상품명 (products.name)
    @Column(nullable = false, length = 100)
    val name: String,

    // 판매 가격 (products.price)
    @Column(nullable = false)
    val price: Long,
) {
    // INSERT 후 DB가 생성한 ID를 Hibernate가 주입하므로 var로 선언한다.
    // 엔티티 외부에서 직접 변경하지 못하도록 setter 접근을 제한한다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
