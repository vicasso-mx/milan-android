package com.doceolab.milan.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryCartRepositoryTest {
    private val repository = InMemoryCartRepository()
    private val product = ProductCatalog.products.first()

    @Test
    fun `agregar el mismo producto incrementa la misma linea`() {
        repository.add(product)
        repository.add(product)

        assertEquals(1, repository.getLines().size)
        assertEquals(2, repository.getLines().single().quantity)
    }

    @Test
    fun `incrementar y disminuir actualiza cantidades y total`() {
        repository.add(product)
        repository.increment(product.id)

        assertEquals(3900L, repository.totalInCents())
        repository.decrement(product.id)
        assertEquals(1950L, repository.totalInCents())
    }

    @Test
    fun `disminuir no permite una cantidad menor que uno`() {
        repository.add(product)
        repository.decrement(product.id)

        assertEquals(1, repository.getLines().single().quantity)
        assertEquals(1950L, repository.totalInCents())
    }

    @Test
    fun `eliminar una linea recalcula el total`() {
        repository.add(product)
        repository.add(ProductCatalog.products[1])
        repository.remove(product.id)

        assertEquals(850L, repository.totalInCents())
    }

    @Test
    fun `limpiar el carrito elimina todas las lineas`() {
        repository.add(product)
        repository.clear()

        assertTrue(repository.getLines().isEmpty())
        assertEquals(0L, repository.totalInCents())
    }
}
