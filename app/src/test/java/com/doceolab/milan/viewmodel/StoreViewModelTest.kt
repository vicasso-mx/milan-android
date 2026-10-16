package com.doceolab.milan.viewmodel

import com.doceolab.milan.data.ProductCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreViewModelTest {
    @Test
    fun `inicia sesion con la cuenta de cliente`() {
        val store = StoreViewModel()

        assertTrue(store.login("cliente@doceolab.com", "Pass@word1"))
        assertEquals("cliente@doceolab.com", store.authenticatedEmail.value)
    }

    @Test
    fun `cuenta alterna tambien inicia sesion como cliente`() {
        val store = StoreViewModel()

        assertTrue(store.login("admin@doceolab.com", "Pass@word1"))
        assertEquals("admin@doceolab.com", store.authenticatedEmail.value)
        store.show(StoreDestination.Orders)
        assertEquals(StoreDestination.Orders, store.destination.value)
    }

    @Test
    fun `rechaza credenciales incorrectas`() {
        val store = StoreViewModel()

        assertFalse(store.login("cliente@doceolab.com", "incorrecta"))
        assertFalse(store.login("otro@doceolab.com", "Pass@word1"))
        assertNull(store.authenticatedEmail.value)
    }

    @Test
    fun `la sesion se conserva al navegar entre destinos`() {
        val store = StoreViewModel()
        store.login("cliente@doceolab.com", "Pass@word1")

        store.show(StoreDestination.Cart)
        store.show(StoreDestination.Orders)
        store.show(StoreDestination.Products)

        assertEquals("cliente@doceolab.com", store.authenticatedEmail.value)
    }

    @Test
    fun `cerrar sesion limpia identidad carrito y compra pendiente`() {
        val store = StoreViewModel()
        store.add(ProductCatalog.products.first())
        store.requestCheckout()
        assertTrue(store.login("cliente@doceolab.com", "Pass@word1"))
        assertEquals(StoreDestination.ReviewOrder, store.destination.value)

        store.logout()

        assertNull(store.authenticatedEmail.value)
        assertTrue(store.cartLines.value.isEmpty())
        assertEquals(0L, store.cartTotalInCents)
        assertEquals(StoreDestination.Products, store.destination.value)
        store.login("cliente@doceolab.com", "Pass@word1")
        assertEquals(StoreDestination.Products, store.destination.value)
    }

    @Test
    fun `bloquea destinos protegidos sin sesion y los permite con sesion`() {
        val store = StoreViewModel()
        store.add(ProductCatalog.products.first())

        store.show(StoreDestination.Orders)
        assertEquals(StoreDestination.Login, store.destination.value)
        store.show(StoreDestination.ReviewOrder)
        assertEquals(StoreDestination.Login, store.destination.value)

        store.login("cliente@doceolab.com", "Pass@word1")
        store.show(StoreDestination.Orders)
        assertEquals(StoreDestination.Orders, store.destination.value)
        store.show(StoreDestination.ReviewOrder)
        assertEquals(StoreDestination.ReviewOrder, store.destination.value)
    }
}
