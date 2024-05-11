package com.example.midespensapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.midespensapp.vista.lista.AnadirCompraActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.midespensapp", appContext.packageName)
    }

    @Test
    fun validarCantidadMinima_CantidadValida() {
        // Valor de entrada válido
        val cantidadMinimaValida = "5"

        AnadirCompraActivity().validarCantidadMinima(cantidadMinimaValida)

        assertTrue(AnadirCompraActivity().validarCantidadMinima(cantidadMinimaValida))
    }

    @Test
    fun validarCantidadMinima_CantidadVacia() {
        // Valor de entrada vacío
        val cantidadMinimaVacia = ""

        assertFalse(AnadirCompraActivity().validarCantidadMinima(cantidadMinimaVacia))
    }

    @Test
    fun validarCantidadMinima_CantidadInvalida() {
        // Valor de entrada inválido (0)
        val cantidadMinimaInvalida = "0"

        assertFalse(AnadirCompraActivity().validarCantidadMinima(cantidadMinimaInvalida))
    }


    @Test
    fun validarCantidadMinima_CantidadValidaEnValorLimite() {
        // Valor de entrada inválido (0)
        val cantidadMinimaInvalida = "1"

        assertTrue(AnadirCompraActivity().validarCantidadMinima(cantidadMinimaInvalida))
    }
}