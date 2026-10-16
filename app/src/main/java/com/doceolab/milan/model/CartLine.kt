package com.doceolab.milan.model

data class CartLine(
    val product: Product,
    val quantity: Int,
) {
    init {
        require(quantity >= 1) { "La cantidad debe ser de al menos uno." }
    }

    val subtotalInCents: Long
        get() = Math.multiplyExact(product.priceInCents, quantity.toLong())
}
