package com.doceolab.milan.model

import java.time.Instant

enum class OrderStatus { Confirmed }

/** Immutable line snapshot captured when a purchase is confirmed. */
data class OrderLine(
    val product: Product,
    val quantity: Int,
) {
    val subtotalInCents: Long
        get() = Math.multiplyExact(product.priceInCents, quantity.toLong())
}

/** In-memory order record. Every product and quantity is copied at confirmation time. */
data class PurchaseOrder(
    val id: String,
    val confirmedAt: Instant,
    val status: OrderStatus,
    val lines: List<OrderLine>,
) {
    val totalInCents: Long
        get() = lines.fold(0L) { total, line -> Math.addExact(total, line.subtotalInCents) }
}
