package com.doceolab.milan.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductCatalogTest {
    @Test
    fun `el catalogo local contiene los doce productos originales`() {
        assertEquals(12, ProductCatalog.products.size)
        assertEquals((1..12).toList(), ProductCatalog.products.map { it.id })
        assertTrue(ProductCatalog.products.all { it.name.isNotBlank() && it.description.isNotBlank() })
    }
}
