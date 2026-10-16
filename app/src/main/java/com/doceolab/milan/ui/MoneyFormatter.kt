package com.doceolab.milan.ui

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Formats integer cents without converting through binary floating point. */
internal fun formatMoney(cents: Long): String = NumberFormat.getCurrencyInstance(
    Locale.Builder().setLanguage("es").setRegion("MX").build(),
).apply {
    currency = Currency.getInstance("MXN")
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}.format(BigDecimal.valueOf(cents, 2)) + " MXN"
