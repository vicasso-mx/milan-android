package com.doceolab.milan.viewmodel

import androidx.lifecycle.ViewModel
import com.doceolab.milan.data.DemoAuthentication
import com.doceolab.milan.data.InMemoryCartRepository
import com.doceolab.milan.data.InMemoryOrderRepository
import com.doceolab.milan.data.ProductCatalog
import com.doceolab.milan.model.CartLine
import com.doceolab.milan.model.PurchaseOrder
import com.doceolab.milan.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class StoreDestination { Products, Cart, Login, Orders, ReviewOrder, ThankYou, OrderDetail }

class StoreViewModel(
    private val cartRepository: InMemoryCartRepository = InMemoryCartRepository(),
    private val authentication: DemoAuthentication = DemoAuthentication(),
    private val orderRepository: InMemoryOrderRepository = InMemoryOrderRepository(),
) : ViewModel() {
    val products: List<Product> = ProductCatalog.products

    private val _cartLines = MutableStateFlow(cartRepository.getLines())
    val cartLines: StateFlow<List<CartLine>> = _cartLines.asStateFlow()

    private val _destination = MutableStateFlow(StoreDestination.Products)
    val destination: StateFlow<StoreDestination> = _destination.asStateFlow()

    private val _authenticatedEmail = MutableStateFlow<String?>(null)
    val authenticatedEmail: StateFlow<String?> = _authenticatedEmail.asStateFlow()

    private val _orders = MutableStateFlow(orderRepository.getOrders())
    val orders: StateFlow<List<PurchaseOrder>> = _orders.asStateFlow()

    private val _activeOrder = MutableStateFlow<PurchaseOrder?>(null)
    val activeOrder: StateFlow<PurchaseOrder?> = _activeOrder.asStateFlow()

    private val _selectedOrder = MutableStateFlow<PurchaseOrder?>(null)
    val selectedOrder: StateFlow<PurchaseOrder?> = _selectedOrder.asStateFlow()

    private var checkoutPending = false

    val cartItemCount: Int
        get() = _cartLines.value.sumOf(CartLine::quantity)

    val cartTotalInCents: Long
        get() = cartRepository.totalInCents()

    fun show(destination: StoreDestination) {
        val protected = destination in setOf(
            StoreDestination.Orders,
            StoreDestination.ReviewOrder,
            StoreDestination.ThankYou,
            StoreDestination.OrderDetail,
        )
        if (protected && _authenticatedEmail.value == null) {
            if (destination == StoreDestination.ReviewOrder && _cartLines.value.isNotEmpty()) {
                checkoutPending = true
            }
            _destination.value = StoreDestination.Login
            return
        }
        if (destination == StoreDestination.ReviewOrder && _cartLines.value.isEmpty()) {
            _destination.value = StoreDestination.Cart
            return
        }
        if (destination == StoreDestination.ThankYou && _activeOrder.value == null) {
            _destination.value = StoreDestination.Products
            return
        }
        if (destination == StoreDestination.OrderDetail && _selectedOrder.value == null) {
            _destination.value = StoreDestination.Orders
            return
        }
        if (destination == StoreDestination.Orders) {
            _selectedOrder.value = null
        }
        _destination.value = destination
    }

    fun login(email: String, password: String): Boolean {
        val account = authentication.authenticate(email, password) ?: return false
        _authenticatedEmail.value = account
        _destination.value = if (checkoutPending && _cartLines.value.isNotEmpty()) {
            StoreDestination.ReviewOrder
        } else {
            StoreDestination.Products
        }
        checkoutPending = false
        return true
    }

    fun logout() {
        _authenticatedEmail.value = null
        cartRepository.clear()
        _cartLines.value = emptyList()
        orderRepository.clear()
        _orders.value = emptyList()
        _selectedOrder.value = null
        _activeOrder.value = null
        checkoutPending = false
        _destination.value = StoreDestination.Products
    }

    fun requestCheckout() {
        if (_cartLines.value.isEmpty()) return
        _activeOrder.value = null
        if (_authenticatedEmail.value == null) {
            checkoutPending = true
            _destination.value = StoreDestination.Login
        } else {
            _destination.value = StoreDestination.ReviewOrder
        }
    }

    /** Confirms at most once: the first call empties the cart and leaves the review destination. */
    fun confirmPurchase(): PurchaseOrder? {
        if (_authenticatedEmail.value == null) {
            checkoutPending = _cartLines.value.isNotEmpty()
            _destination.value = StoreDestination.Login
            return null
        }
        if (_destination.value != StoreDestination.ReviewOrder) return null
        if (_cartLines.value.isEmpty()) {
            _destination.value = StoreDestination.Cart
            return null
        }

        val order = orderRepository.createOrder(_cartLines.value.toList())
        _orders.value = orderRepository.getOrders()
        _activeOrder.value = order
        _selectedOrder.value = null
        cartRepository.clear()
        _cartLines.value = emptyList()
        checkoutPending = false
        _destination.value = StoreDestination.ThankYou
        return order
    }

    fun selectOrder(orderId: String): Boolean {
        if (_authenticatedEmail.value == null) {
            _destination.value = StoreDestination.Login
            return false
        }
        val order = orderRepository.findOrder(orderId) ?: return false
        _selectedOrder.value = order
        _destination.value = StoreDestination.OrderDetail
        return true
    }

    fun add(product: Product) = updateCart { cartRepository.add(product) }
    fun increment(productId: Int) = updateCart { cartRepository.increment(productId) }
    fun decrement(productId: Int) = updateCart { cartRepository.decrement(productId) }
    fun remove(productId: Int) = updateCart { cartRepository.remove(productId) }

    private inline fun updateCart(action: () -> Unit) {
        action()
        _cartLines.value = cartRepository.getLines()
    }
}
