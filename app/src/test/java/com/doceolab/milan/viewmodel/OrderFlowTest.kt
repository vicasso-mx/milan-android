package com.doceolab.milan.viewmodel

import com.doceolab.milan.data.InMemoryOrderRepository
import com.doceolab.milan.data.ProductCatalog
import com.doceolab.milan.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class OrderFlowTest {
    @Test
    fun `permite revisar el pedido si hay sesion y productos`() {
        val store = authenticatedStore()
        store.add(ProductCatalog.products.first())

        store.requestCheckout()

        assertEquals(StoreDestination.ReviewOrder, store.destination.value)
    }

    @Test
    fun `solicita inicio de sesion antes de revisar el pedido`() {
        val store = StoreViewModel()
        store.add(ProductCatalog.products.first())

        store.requestCheckout()

        assertEquals(StoreDestination.Login, store.destination.value)
        assertTrue(store.login("cliente@doceolab.com", "Pass@word1"))
        assertEquals(StoreDestination.ReviewOrder, store.destination.value)
        assertEquals(1, store.cartLines.value.single().quantity)
    }

    @Test
    fun `rechaza confirmar con carrito vacio`() {
        val store = authenticatedStore()
        store.requestCheckout()
        store.show(StoreDestination.ReviewOrder)

        assertEquals(StoreDestination.Cart, store.destination.value)
        assertNull(store.confirmPurchase())
        assertTrue(store.orders.value.isEmpty())
    }

    @Test
    fun `crea pedido confirmado con identificador y fecha`() {
        val store = authenticatedStore()
        store.add(ProductCatalog.products.first())
        store.requestCheckout()

        val order = store.confirmPurchase()

        assertNotNull(order)
        assertTrue(order!!.id.isNotBlank())
        assertNotNull(order.confirmedAt)
        assertEquals(OrderStatus.Confirmed, order.status)
        assertEquals(StoreDestination.ThankYou, store.destination.value)
    }

    @Test
    fun `calcula total con las cantidades confirmadas`() {
        val store = authenticatedStore()
        store.add(ProductCatalog.products[0])
        store.add(ProductCatalog.products[0])
        store.add(ProductCatalog.products[1])
        store.requestCheckout()

        val order = store.confirmPurchase()!!

        assertEquals(4750L, order.totalInCents)
    }

    @Test
    fun `pedido conserva cantidades y precios como instantanea`() {
        val store = authenticatedStore()
        val product = ProductCatalog.products.first()
        store.add(product)
        store.add(product)
        store.requestCheckout()
        val order = store.confirmPurchase()!!

        assertEquals(2, order.lines.single().quantity)
        assertEquals(product.priceInCents, order.lines.single().product.priceInCents)
        assertEquals(product.name, order.lines.single().product.name)
        assertEquals(3900L, order.lines.single().subtotalInCents)
    }

    @Test
    fun `una compra confirmada no se puede confirmar dos veces`() {
        val store = authenticatedStore()
        store.add(ProductCatalog.products.first())
        store.requestCheckout()

        assertNotNull(store.confirmPurchase())
        assertNull(store.confirmPurchase())
        assertEquals(1, store.orders.value.size)
    }

    @Test
    fun `vaciar el carrito despues de confirmar`() {
        val store = authenticatedStore()
        store.add(ProductCatalog.products.first())
        store.requestCheckout()

        store.confirmPurchase()

        assertTrue(store.cartLines.value.isEmpty())
        assertEquals(0L, store.cartTotalInCents)
    }

    @Test
    fun `agrega el pedido confirmado al historial`() {
        val store = authenticatedStore()
        store.add(ProductCatalog.products.first())
        store.requestCheckout()
        val order = store.confirmPurchase()!!

        assertEquals(order, store.orders.value.single())
    }

    @Test
    fun `conserva varios pedidos durante la sesion`() {
        val store = authenticatedStore()
        val first = confirm(store, ProductCatalog.products[0])
        val second = confirm(store, ProductCatalog.products[1])

        assertEquals(2, store.orders.value.size)
        assertTrue(store.orders.value.any { it.id == first.id })
        assertTrue(store.orders.value.any { it.id == second.id })
    }

    @Test
    fun `ordena los pedidos mas recientes primero`() {
        val clock = MutableClock(Instant.parse("2026-01-01T10:00:00Z"))
        val store = StoreViewModel(orderRepository = InMemoryOrderRepository(clock))
        store.login("cliente@doceolab.com", "Pass@word1")
        val first = confirm(store, ProductCatalog.products[0])
        clock.current = Instant.parse("2026-01-02T10:00:00Z")
        val second = confirm(store, ProductCatalog.products[1])

        assertEquals(listOf(second.id, first.id), store.orders.value.map { it.id })
    }

    @Test
    fun `permite consultar el detalle de un pedido`() {
        val store = authenticatedStore()
        val order = confirm(store, ProductCatalog.products.first())

        assertTrue(store.selectOrder(order.id))
        assertEquals(StoreDestination.OrderDetail, store.destination.value)
        assertEquals(order, store.selectedOrder.value)
        assertEquals("AeroMILA Halcón P2", store.selectedOrder.value!!.lines.single().product.name)
    }

    @Test
    fun `cerrar sesion elimina historial detalle y pedido activo`() {
        val store = authenticatedStore()
        val order = confirm(store, ProductCatalog.products.first())
        store.selectOrder(order.id)

        store.logout()

        assertTrue(store.orders.value.isEmpty())
        assertNull(store.selectedOrder.value)
        assertNull(store.activeOrder.value)
        assertEquals(StoreDestination.Products, store.destination.value)
    }

    @Test
    fun `el historial esta vacio al iniciar otra sesion`() {
        val store = authenticatedStore()
        confirm(store, ProductCatalog.products.first())
        store.logout()
        store.login("cliente@doceolab.com", "Pass@word1")

        assertTrue(store.orders.value.isEmpty())
    }

    @Test
    fun `restringe historial y pedidos tras cerrar sesion`() {
        val store = authenticatedStore()
        val order = confirm(store, ProductCatalog.products.first())
        store.logout()

        assertFalse(store.selectOrder(order.id))
        store.show(StoreDestination.Orders)
        assertEquals(StoreDestination.Login, store.destination.value)
        store.show(StoreDestination.OrderDetail)
        assertEquals(StoreDestination.Login, store.destination.value)
        assertTrue(store.orders.value.isEmpty())
    }

    private fun authenticatedStore(): StoreViewModel = StoreViewModel().also {
        assertTrue(it.login("cliente@doceolab.com", "Pass@word1"))
    }

    private fun confirm(store: StoreViewModel, product: com.doceolab.milan.model.Product) = product.let {
        store.add(it)
        store.requestCheckout()
        store.confirmPurchase()!!
    }

    private class MutableClock(var current: Instant) : Clock() {
        override fun instant(): Instant = current
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
    }
}
