package com.example.camara

import com.example.camara.data.local.MovimientoDao
import com.example.camara.data.local.MovimientoEntity
import com.example.camara.data.local.ProductoDao
import com.example.camara.data.local.ProductoEntity
import com.example.camara.data.repository.ProductoRepository
import com.example.camara.ui.producto.ProductoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

private class FakeDao : ProductoDao {
    val datos = MutableStateFlow<List<ProductoEntity>>(emptyList())
    override fun observarProductos(filtro: String): Flow<List<ProductoEntity>> = datos
    override suspend fun obtenerPorId(id: Int) = datos.value.firstOrNull { it.id == id }
    override suspend fun obtenerPorCodigo(codigo: String) = datos.value.firstOrNull { it.codigo.equals(codigo, true) }
    override suspend fun insertar(producto: ProductoEntity): Long {
        datos.value = datos.value + producto.copy(id = datos.value.size + 1); return 1
    }
    override suspend fun actualizar(producto: ProductoEntity) {
        datos.value = datos.value.map { if (it.id == producto.id) producto else it }
    }
    override suspend fun descontar(id: Int, n: Int): Int {
        val p = datos.value.firstOrNull { it.id == id && it.cantidad >= n } ?: return 0
        datos.value = datos.value.map { if (it.id == id) p.copy(cantidad = p.cantidad - n) else it }; return 1
    }
    override suspend fun eliminar(producto: ProductoEntity) { datos.value = datos.value - producto }
}

private class FakeMovimientoDao : MovimientoDao {
    val lista = mutableListOf<MovimientoEntity>()
    override suspend fun insertar(movimiento: MovimientoEntity) { lista += movimiento }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {
    private lateinit var dao: FakeDao
    private lateinit var vm: ProductoViewModel
    private lateinit var movs: FakeMovimientoDao

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dao = FakeDao(); movs = FakeMovimientoDao(); vm = ProductoViewModel(ProductoRepository(dao, movs))
    }
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun camposVacios_muestranErrores() {
        vm.guardar()
        val s = vm.formState.value
        assertNotNull(s.codigoError); assertNotNull(s.nombreError); assertNotNull(s.categoriaError)
        assertNotNull(s.cantidadError); assertNotNull(s.stockMinimoError)
        assertTrue(dao.datos.value.isEmpty())
    }

    @Test fun cantidadNegativa_oNoNumerica_esInvalida() {
        vm.onCodigoChange("A1"); vm.onNombreChange("X"); vm.onCategoriaChange("C"); vm.onStockMinimoChange("2")
        vm.onCantidadChange("-1"); vm.guardar()
        assertNotNull(vm.formState.value.cantidadError)
        vm.onCantidadChange("abc"); vm.guardar()
        assertNotNull(vm.formState.value.cantidadError)
    }

    @Test fun productoValido_seGuarda_ycodigoDuplicadoSeRechaza() {
        fun llenar() {
            vm.iniciarFormulario(null)
            vm.onCodigoChange("A1"); vm.onNombreChange("Aceite"); vm.onCategoriaChange("Lubricantes")
            vm.onCantidadChange("5"); vm.onStockMinimoChange("2")
        }
        llenar(); vm.guardar()
        assertEquals(1, dao.datos.value.size)
        assertTrue(vm.formState.value.guardadoExitosamente)

        llenar(); vm.guardar()
        assertEquals(1, dao.datos.value.size)
        assertNotNull(vm.formState.value.codigoError)
    }

    @Test fun compra_descuentaStock_registraMovimiento_yRechazaExceso() {
        dao.datos.value = listOf(ProductoEntity(1, "A1", "Aceite", "Lub", 5, 2))
        vm.iniciarFormulario(1)
        vm.onCantidadCompraChange("9"); vm.solicitarCompra()
        assertNotNull(vm.formState.value.compraError)           // más que el stock
        vm.onCantidadCompraChange("3"); vm.solicitarCompra()
        assertTrue(vm.formState.value.confirmandoCompra)
        vm.confirmarCompra("cliente")
        assertEquals(2, dao.datos.value.first().cantidad)
        assertEquals(1, movs.lista.size)
        assertEquals(5, movs.lista[0].existenciaAnterior)
        assertEquals(2, movs.lista[0].existenciaResultante)
    }
}
