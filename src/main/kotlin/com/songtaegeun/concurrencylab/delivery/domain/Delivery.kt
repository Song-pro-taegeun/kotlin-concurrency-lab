package com.songtaegeun.concurrencylab.delivery.domain

import com.songtaegeun.concurrencylab.product.domain.Product
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

// 상품별 배송 예정 정보를 관리하는 엔티티
@Entity
@Table(name = "deliveries")
class Delivery(
    // 상품 FK (deliveries.product_id), 상품별 배송 정보 한 건을 보장한다.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_delivery_product"),
    )
    val product: Product,

    // 주문일로부터 배송까지 필요한 일수 (deliveries.lead_time_days)
    @Column(nullable = false)
    val leadTimeDays: Int,
) {
    // INSERT 후 DB가 생성한 ID를 Hibernate가 주입하므로 var로 선언한다.
    // 엔티티 외부에서 직접 변경하지 못하도록 setter 접근을 제한한다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
