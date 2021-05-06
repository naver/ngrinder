import net.grinder.script.Grinder.grinder
import net.grinder.script.Test

class TestRunner {

    private val test = Test(1, "Test")

    init {
        test.record(this::doTest)
    }

    fun doTest() {
        grinder.logger.info("It works!")
    }
}

// Kotlin test script MUST be ended up with TestRunner class
TestRunner::class
