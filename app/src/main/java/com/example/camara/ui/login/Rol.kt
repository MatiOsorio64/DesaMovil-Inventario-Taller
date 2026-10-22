package com.example.camara.ui.login

/** ADMIN: gestiona productos. CLIENTE: solo consulta (lista, detalle y QR). */
enum class Rol(val etiqueta: String) {
    ADMIN("Administrador"),
    CLIENTE("Cliente")
}
