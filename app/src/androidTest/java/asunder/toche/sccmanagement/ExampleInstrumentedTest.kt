package asunder.toche.sccmanagement

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented menu_drawer, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under menu_drawer.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("asunder.toche.scc_management", appContext.packageName)
    }
}
