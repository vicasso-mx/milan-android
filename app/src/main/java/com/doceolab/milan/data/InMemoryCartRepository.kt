package com.doceolab.milan.data

import com.doceolab.milan.model.CartLine
import com.doceolab.milan.model.Product

/** Carrito temporal en memoria; agregar un producto existente incrementa su cantidad. */
class InMemoryCartRepository {
    private val linesByProductId = linkedMapOf<Int, CartLine>()

    fun getLines(): List<CartLine> = linesByProductId.values.toList()

    fun add(product: Product) {
        val current = linesByProductId[product.id]
        linesByProductId[product.id] = if (current == null) {
            CartLine(product, 1)
        } else {
            current.copy(quantity = Math.addExact(current.quantity, 1))
        }
    }

    fun increment(productId: Int) {
        val current = requireLine(productId)
        linesByProductId[productId] = current.copy(quantity = Math.addExact(current.quantity, 1))
    }

    fun decrement(productId: Int) {
        val current = requireLine(productId)
        if (current.quantity > 1) {
            linesByProductId[productId] = current.copy(quantity = current.quantity - 1)
        }
    }

    fun remove(productId: Int) {
        linesByProductId.remove(productId)
    }

    fun clear() {
        linesByProductId.clear()
    }

    fun totalInCents(): Long = linesByProductId.values.fold(0L) { total, line ->
        Math.addExact(total, line.subtotalInCents)
    }

    private fun requireLine(productId: Int): CartLine =
        requireNotNull(linesByProductId[productId]) { "El producto no está en el carrito." }
}
