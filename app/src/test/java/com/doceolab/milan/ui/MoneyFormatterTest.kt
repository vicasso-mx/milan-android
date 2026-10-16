package com.doceolab.milan.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {
    @Test
    fun `formatea centavos como pesos mexicanos sin perder precision`() {
        assertEquals("$19.99 MXN", formatMoney(1999))
        assertEquals("$0.01 MXN", formatMoney(1))
        assertEquals("$0.00 MXN", formatMoney(0))
    }
}
