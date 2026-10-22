package com.example.camara.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.camara.data.local.AppDatabase
import com.example.camara.data.repository.ProductoRepository
import com.example.camara.ui.AppViewModelFactory
import com.example.camara.ui.login.LoginScreen
import com.example.camara.ui.login.LoginViewModel
import com.example.camara.ui.login.Rol
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.camara.ui.menu.MenuScreen
import com.example.camara.ui.producto.ListaProductosScreen
import com.example.camara.ui.producto.ProductoFormScreen
import com.example.camara.ui.producto.ProductoViewModel
import com.example.camara.ui.scanner.QrScannerScreen
import com.example.camara.ui.scanner.QrViewModel
import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

object Rutas {
    const val LOGIN = "login"
    const val MENU = "menu"
    const val LISTA = "lista"
    const val SCANNER = "scanner"
    const val FORM = "form?id={id}&codigo={codigo}"
    fun form(id: Int? = null, codigo: String? = null): String {
        val ruta = StringBuilder("form?id=${id ?: -1}")
        if (!codigo.isNullOrBlank()) ruta.append("&codigo=${Uri.encode(codigo)}")
        return ruta.toString()
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val context = LocalContext.current.applicationContext
    val factory = remember {
        AppViewModelFactory(AppDatabase.getInstance(context).let { ProductoRepository(it.productoDao(), it.movimientoDao()) })
    }
    val productoVm: ProductoViewModel = viewModel(factory = factory)
    val loginVm: LoginViewModel = viewModel()
    var rolActual by rememberSaveable { mutableStateOf(Rol.CLIENTE.name) }   // rol de la sesión (mínimo privilegio por defecto)
    val esAdmin = rolActual == Rol.ADMIN.name

    NavHost(
        navController = nav,
        startDestination = Rutas.LOGIN,
        enterTransition = { slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(200)) }
    ) {

        composable(Rutas.LOGIN) {
            LoginScreen(loginVm, onLoginOk = { rol ->
                rolActual = rol.name
                nav.navigate(Rutas.MENU) { popUpTo(Rutas.LOGIN) { inclusive = true } }
            })
        }

        composable(Rutas.MENU) {
            MenuScreen(
                rol = Rol.valueOf(rolActual),
                onProductos = { nav.navigate(Rutas.LISTA) },
                onEscanear = { nav.navigate(Rutas.SCANNER) },
                onCerrarSesion = { nav.navigate(Rutas.LOGIN) { popUpTo(Rutas.MENU) { inclusive = true } } }
            )
        }

        composable(Rutas.LISTA) {
            ListaProductosScreen(
                viewModel = productoVm,
                esAdmin = esAdmin,
                onAgregar = { nav.navigate(Rutas.form()) },
                onEditar = { id -> nav.navigate(Rutas.form(id = id)) },
                onEscanear = { nav.navigate(Rutas.SCANNER) },
                onVolver = { nav.popBackStack() }
            )
        }

        composable(
            Rutas.FORM,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType; defaultValue = -1 },
                navArgument("codigo") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            val id = entry.arguments?.getInt("id")?.takeIf { it >= 0 }
            val codigo = entry.arguments?.getString("codigo")?.ifBlank { null }
            ProductoFormScreen(productoVm, id, codigo, soloLectura = !esAdmin, onVolver = { nav.popBackStack() })
        }

        composable(Rutas.SCANNER) {
            val qrVm: QrViewModel = viewModel(factory = factory)
            QrScannerScreen(
                viewModel = qrVm,
                esAdmin = esAdmin,
                onVerProducto = { id -> nav.navigate(Rutas.form(id = id)) },
                onRegistrarCodigo = { cod -> nav.navigate(Rutas.form(codigo = cod)) },
                onVolver = { nav.popBackStack() }
            )
        }
    }
}
