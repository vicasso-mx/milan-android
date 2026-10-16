package com.doceolab.milan.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.doceolab.milan.R
import com.doceolab.milan.model.CartLine
import com.doceolab.milan.model.OrderStatus
import com.doceolab.milan.model.PurchaseOrder
import com.doceolab.milan.model.Product
import com.doceolab.milan.ui.theme.MilanBlue
import com.doceolab.milan.viewmodel.StoreDestination
import com.doceolab.milan.viewmodel.StoreViewModel
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilanApp(viewModel: StoreViewModel) {
    val destination by viewModel.destination.collectAsState()
    val cartLines by viewModel.cartLines.collectAsState()
    val authenticatedEmail by viewModel.authenticatedEmail.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()

    BackHandler(enabled = destination != StoreDestination.Products) {
        when (destination) {
            StoreDestination.ReviewOrder -> viewModel.show(StoreDestination.Cart)
            StoreDestination.OrderDetail -> viewModel.show(StoreDestination.Orders)
            else -> viewModel.show(StoreDestination.Products)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.milan_brand),
                            contentDescription = "MILAN",
                            modifier = Modifier.height(32.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Text("  MILAN", color = MilanBlue, fontWeight = FontWeight.Bold)
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = destination == StoreDestination.Products,
                    onClick = { viewModel.show(StoreDestination.Products) },
                    icon = { Text("▦") },
                    label = { Text("Productos") },
                )
                NavigationBarItem(
                    selected = destination == StoreDestination.Cart,
                    onClick = { viewModel.show(StoreDestination.Cart) },
                    icon = {
                        BadgedBox(badge = {
                            if (viewModel.cartItemCount > 0) Badge { Text(viewModel.cartItemCount.toString()) }
                        }) { Text("🛒") }
                    },
                    label = { Text("Carrito") },
                )
                if (authenticatedEmail == null) {
                    NavigationBarItem(
                        selected = destination == StoreDestination.Login,
                        onClick = { viewModel.show(StoreDestination.Login) },
                        icon = { Text("👤") },
                        label = { Text("Iniciar sesión") },
                    )
                } else {
                    NavigationBarItem(
                        selected = destination == StoreDestination.Orders,
                        onClick = { viewModel.show(StoreDestination.Orders) },
                        icon = { Text("▤") },
                        label = { Text("Mis pedidos") },
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = viewModel::logout,
                        icon = { Text("↪") },
                        label = { Text("Cerrar sesión") },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (destination) {
            StoreDestination.Products -> ProductList(
                products = viewModel.products,
                onAdd = viewModel::add,
                modifier = Modifier.padding(innerPadding),
            )
            StoreDestination.Cart -> CartScreen(
                lines = cartLines,
                totalInCents = viewModel.cartTotalInCents,
                onIncrement = viewModel::increment,
                onDecrement = viewModel::decrement,
                onRemove = viewModel::remove,
                onContinueShopping = { viewModel.show(StoreDestination.Products) },
                onCheckout = viewModel::requestCheckout,
                modifier = Modifier.padding(innerPadding),
            )
            StoreDestination.Login -> LoginScreen(
                onLogin = viewModel::login,
                onCancel = { viewModel.show(StoreDestination.Products) },
                modifier = Modifier.padding(innerPadding),
            )
            StoreDestination.Orders -> OrdersScreen(
                orders = orders,
                onSelect = { viewModel.selectOrder(it) },
                modifier = Modifier.padding(innerPadding),
            )
            StoreDestination.ReviewOrder -> ReviewOrderScreen(
                lines = cartLines,
                totalInCents = viewModel.cartTotalInCents,
                onReturn = { viewModel.show(StoreDestination.Cart) },
                onPay = { viewModel.confirmPurchase() },
                modifier = Modifier.padding(innerPadding),
            )
            StoreDestination.ThankYou -> activeOrder?.let { order ->
                ThankYouScreen(
                    order = order,
                    onContinueShopping = { viewModel.show(StoreDestination.Products) },
                    onOrders = { viewModel.show(StoreDestination.Orders) },
                    modifier = Modifier.padding(innerPadding),
                )
            } ?: ProductList(viewModel.products, viewModel::add, Modifier.padding(innerPadding))
            StoreDestination.OrderDetail -> selectedOrder?.let { order ->
                OrderDetailScreen(
                    order = order,
                    onReturn = { viewModel.show(StoreDestination.Orders) },
                    modifier = Modifier.padding(innerPadding),
                )
            } ?: MissingOrderScreen(
                onReturn = { viewModel.show(StoreDestination.Orders) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun ProductList(
    products: List<Product>,
    onAdd: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.milan_banner),
                contentDescription = "MILAN: drones y novedades",
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentScale = ContentScale.Crop,
            )
            Text("Productos", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
        }
        items(products, key = Product::id) { product ->
            ProductCard(product, onAdd)
        }
    }
}

@Composable
private fun ProductCard(product: Product, onAdd: (Product) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Image(
                painter = painterResource(product.drawableId()),
                contentDescription = product.name,
                modifier = Modifier.fillMaxWidth().height(190.dp),
                contentScale = ContentScale.Fit,
            )
            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(product.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(formatMoney(product.priceInCents), style = MaterialTheme.typography.titleLarge, color = MilanBlue, fontWeight = FontWeight.Bold)
                Button(onClick = { onAdd(product) }) { Text("Agregar al carrito") }
            }
        }
    }
}

@Composable
private fun CartScreen(
    lines: List<CartLine>,
    totalInCents: Long,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onContinueShopping: () -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (lines.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text("Explora los productos MILAN y agrega tus favoritos.", modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
            Button(onClick = onContinueShopping, modifier = Modifier.padding(top = 20.dp)) { Text("Seguir comprando") }
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Carrito", style = MaterialTheme.typography.headlineSmall) }
            items(lines, key = { it.product.id }) { line ->
                CartLineCard(line, onIncrement, onDecrement, onRemove)
            }
            item {
                Button(onClick = onContinueShopping, modifier = Modifier.fillMaxWidth()) { Text("Seguir comprando") }
            }
        }
        Surface(shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(formatMoney(totalInCents), style = MaterialTheme.typography.titleLarge, color = MilanBlue, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onCheckout, enabled = lines.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text("Finalizar compra")
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    onLogin: (String, String) -> Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Esta es una aplicación de aprendizaje. Puede iniciar sesión con las siguientes credenciales:\n\n" +
                "Usuario: cliente@doceolab.com o admin@doceolab.com\n\nContraseña: Pass@word1",
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = {
                val accepted = onLogin(email, password)
                password = ""
                error = if (accepted) null else "El correo o la contraseña son incorrectos. Verifica tus datos e inténtalo de nuevo."
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Iniciar sesión") }
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Volver al catálogo")
        }
    }
}

@Composable
private fun ReviewOrderScreen(
    lines: List<CartLine>,
    totalInCents: Long,
    onReturn: () -> Unit,
    onPay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Revisar pedido", style = MaterialTheme.typography.headlineSmall) }
            items(lines, key = { it.product.id }) { line ->
                OrderLineCard(
                    productName = line.product.name,
                    quantity = line.quantity,
                    unitPriceInCents = line.product.priceInCents,
                    subtotalInCents = line.subtotalInCents,
                )
            }
            item {
                TextButton(onClick = onReturn) { Text("Regresar al carrito") }
            }
        }
        Surface(shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(formatMoney(totalInCents), style = MaterialTheme.typography.titleLarge, color = MilanBlue, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onPay, enabled = lines.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text("Pagar ahora")
                }
            }
        }
    }
}

@Composable
private fun ThankYouScreen(
    order: PurchaseOrder,
    onContinueShopping: () -> Unit,
    onOrders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("¡Gracias por tu pedido!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Text("Tu compra se confirmó correctamente.", modifier = Modifier.padding(top = 12.dp), textAlign = TextAlign.Center)
        Text("Pedido: ${order.id}", modifier = Modifier.padding(top = 20.dp), style = MaterialTheme.typography.bodyLarge)
        Text("Total: ${formatMoney(order.totalInCents)}", modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleLarge, color = MilanBlue, fontWeight = FontWeight.Bold)
        Button(onClick = onContinueShopping, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text("Seguir comprando")
        }
        TextButton(onClick = onOrders) { Text("Mis pedidos") }
    }
}

@Composable
private fun OrdersScreen(
    orders: List<PurchaseOrder>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (orders.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Mis pedidos", style = MaterialTheme.typography.headlineSmall)
            Text("Todavía no tiene pedidos", modifier = Modifier.padding(top = 12.dp))
        }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Mis pedidos", style = MaterialTheme.typography.headlineSmall) }
        items(orders, key = PurchaseOrder::id) { order ->
            Card(
                onClick = { onSelect(order.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pedido ${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Fecha: ${formatDate(order)}", modifier = Modifier.padding(top = 6.dp))
                    Text("Total: ${formatMoney(order.totalInCents)}", modifier = Modifier.padding(top = 4.dp), color = MilanBlue)
                    Text("Estado: ${order.status.displayName()}", modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun OrderDetailScreen(
    order: PurchaseOrder,
    onReturn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Detalle del pedido", style = MaterialTheme.typography.headlineSmall)
                Text("Identificador: ${order.id}", modifier = Modifier.padding(top = 8.dp))
                Text("Fecha: ${formatDate(order)}", modifier = Modifier.padding(top = 4.dp))
                Text("Estado: ${order.status.displayName()}", modifier = Modifier.padding(top = 4.dp))
            }
            items(order.lines, key = { it.product.id }) { line ->
                OrderLineCard(
                    productName = line.product.name,
                    quantity = line.quantity,
                    unitPriceInCents = line.product.priceInCents,
                    subtotalInCents = line.subtotalInCents,
                )
            }
        }
        Surface(shadowElevation = 6.dp, color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(formatMoney(order.totalInCents), style = MaterialTheme.typography.titleLarge, color = MilanBlue, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onReturn, modifier = Modifier.fillMaxWidth()) { Text("Volver a Mis pedidos") }
            }
        }
    }
}

@Composable
private fun OrderLineCard(
    productName: String,
    quantity: Int,
    unitPriceInCents: Long,
    subtotalInCents: Long,
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Cantidad: $quantity")
            Text("Precio unitario: ${formatMoney(unitPriceInCents)}")
            Text("Subtotal: ${formatMoney(subtotalInCents)}", color = MilanBlue, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MissingOrderScreen(onReturn: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No se encontró el pedido", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onReturn, modifier = Modifier.padding(top = 16.dp)) { Text("Volver a Mis pedidos") }
    }
}

private fun formatDate(order: PurchaseOrder): String = DateTimeFormatter
    .ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"))
    .withZone(ZoneId.systemDefault())
    .format(order.confirmedAt)

private fun OrderStatus.displayName(): String = when (this) {
    OrderStatus.Confirmed -> "Confirmado"
}

@Composable
private fun CartLineCard(
    line: CartLine,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(line.product.drawableId()),
                contentDescription = line.product.name,
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(line.product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Precio unitario: ${formatMoney(line.product.priceInCents)}", style = MaterialTheme.typography.bodySmall)
                Text("Importe: ${formatMoney(line.subtotalInCents)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.TextButton(
                        onClick = { onDecrement(line.product.id) },
                        enabled = line.quantity > 1,
                    ) { Text("−") }
                    Text("${line.quantity}")
                    androidx.compose.material3.TextButton(onClick = { onIncrement(line.product.id) }) { Text("+") }
                    androidx.compose.material3.TextButton(onClick = { onRemove(line.product.id) }) { Text("Eliminar") }
                }
            }
        }
    }
}

private fun Product.drawableId(): Int = when (imageName) {
    "product_01" -> R.drawable.product_01
    "product_02" -> R.drawable.product_02
    "product_03" -> R.drawable.product_03
    "product_04" -> R.drawable.product_04
    "product_05" -> R.drawable.product_05
    "product_06" -> R.drawable.product_06
    "product_07" -> R.drawable.product_07
    "product_08" -> R.drawable.product_08
    "product_09" -> R.drawable.product_09
    "product_10" -> R.drawable.product_10
    "product_11" -> R.drawable.product_11
    else -> R.drawable.product_12
}

private fun formatMoney(cents: Long): String = NumberFormat.getCurrencyInstance(
    Locale.Builder().setLanguage("es").setRegion("MX").build(),
).apply {
    currency = java.util.Currency.getInstance("MXN")
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}.format(cents / 100.0) + " MXN"
