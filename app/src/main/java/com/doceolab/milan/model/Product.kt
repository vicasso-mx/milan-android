package com.doceolab.milan.model

/** El precio se representa en centavos para evitar errores de redondeo binario. */
data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val priceInCents: Long,
    val imageName: String,
)
