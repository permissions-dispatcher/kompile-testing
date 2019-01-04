package kompile.testing

import org.junit.Assert.assertNotNull
import org.junit.Test

class CommandsTest {
    @Test
    fun kotlinc() {
        assertNotNull(kotlinc())
    }
}