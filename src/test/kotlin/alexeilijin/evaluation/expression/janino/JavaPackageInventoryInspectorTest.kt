package alexeilijin.evaluation.expression.janino

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.time.measureTimedValue

class JavaBaseModuleClassInventoryInspectorTest {

    @Disabled
    @Test
    fun `should return java_lang classes list`() {
        val classNames = measureTimedValue { JavaBaseModuleClassInventoryInspector.listClasses() }
        println("Measured time: ${classNames.duration}")
        classNames.value.map { it.name.split(".").last() }.sorted().forEach { println(it) }
        assertTrue(classNames.value.isNotEmpty(), "Unexpected class count")
    }
}