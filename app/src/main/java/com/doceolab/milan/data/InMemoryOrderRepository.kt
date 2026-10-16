package com.doceolab.milan.data

import com.doceolab.milan.model.OrderLine
import com.doceolab.milan.model.OrderStatus
import com.doceolab.milan.model.PurchaseOrder
import com.doceolab.milan.model.CartLine
import java.time.Clock
import java.util.Collections
import java.util.UUID

/** Keeps confirmed orders in memory for the current authenticated session. */
class InMemoryOrderRepository(
    private val clock: Clock = Clock.systemUTC(),
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) {
    private val orders = mutableListOf<PurchaseOrder>()

    fun createOrder(cartLines: List<CartLine>): PurchaseOrder {
        require(cartLines.isNotEmpty()) { "No se puede confirmar un carrito vacío." }
        val snapshot = cartLines.map { line ->
            OrderLine(product = line.product.copy(), quantity = line.quantity)
        }
        val order = PurchaseOrder(
            id = idGenerator(),
            confirmedAt = clock.instant(),
            status = OrderStatus.Confirmed,
            lines = Collections.unmodifiableList(snapshot),
        )
        orders += order
        return order
    }

    /** Newest first; reverse insertion order breaks timestamp ties in favor of the latest order. */
    fun getOrders(): List<PurchaseOrder> = orders.asReversed()
        .sortedByDescending(PurchaseOrder::confirmedAt)
        .toList()

    fun findOrder(id: String): PurchaseOrder? = orders.firstOrNull { it.id == id }

    fun clear() = orders.clear()
}
