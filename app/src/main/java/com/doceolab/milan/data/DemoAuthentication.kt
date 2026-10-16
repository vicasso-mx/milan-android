package com.doceolab.milan.data

/** Autenticación local de demostración. No representa seguridad para producción. */
class DemoAuthentication {
    fun authenticate(email: String, password: String): String? {
        val normalizedEmail = email.trim().lowercase()
        return normalizedEmail.takeIf {
            it in DEMO_EMAILS && password == DEMO_PASSWORD
        }
    }

    private companion object {
        const val DEMO_PASSWORD = "Pass@word1"
        val DEMO_EMAILS = setOf("cliente@doceolab.com", "admin@doceolab.com")
    }
}
