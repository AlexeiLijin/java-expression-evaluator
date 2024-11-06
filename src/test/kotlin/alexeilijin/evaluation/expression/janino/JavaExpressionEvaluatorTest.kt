package alexeilijin.evaluation.expression.janino

import org.codehaus.commons.compiler.CompileException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.InvocationTargetException
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.time.measureTimedValue

class JavaExpressionEvaluatorTest {

    private val defaultPersonId = "8e5694b6-d9a0-4cc7-991a-b6e84a4fbbfd"
    private val defaultPerson = Person(
        id = UUID.fromString(defaultPersonId),
        name = "Arno Unkrig",
        age = 54,
        birthDate = ZonedDateTime.parse("1970-01-01T00:00:00Z"),
        employed = true
    )
    private val defaultExpression = """
        person.getId().equals(UUID.fromString("$defaultPersonId")) &&
        person.getName().contains("Unkrig") &&
        person.getBirthDate().isBefore(ZonedDateTime.parse("2000-01-01T00:00:00Z")) &&
        person.getAge() > 27 &&
        person.getEmployed()
    """.trimIndent()
    private val defaultResultType = Boolean::class.java
    private val defaultImports = listOf(
        "import java.time.ZonedDateTime;",
        "import java . \tutil . UUID;",
    )
    private val defaultArgumentTypes = mapOf("person" to Person::class.java, "person" to Person::class.java)

    @Test
    fun `should evaluate boolean expression`() {
        val evaluator = JavaExpressionEvaluator(defaultExpression, defaultResultType, defaultImports, defaultArgumentTypes)
        assertEquals(true, evaluator.eval(defaultPerson))
    }

    @Test
    fun `should evaluate arithmetic exception`() {
        val evaluator = JavaExpressionEvaluator("1 + 1", Integer::class.java)
        assertEquals(2, evaluator.eval())
    }

    @Test
    fun `should evaluate expression with void result type`() {
        val evaluator = JavaExpressionEvaluator(
            """System.out.println("Hello world!")""", Void.TYPE, listOf("import java.lang.System;")
        )
        assertEquals(null, evaluator.eval())
    }

    @Test
    fun `shouldn't evaluate expression with not imported java_lang class`() {
        val exception = assertThrows<IllegalArgumentException> {
            JavaExpressionEvaluator("""System.exit(1)""", Void.TYPE).eval()
        }
        println(exception.message)
    }

    @Test
    fun `shouldn't evaluate expression with import`() {
        val exception = assertThrows<IllegalArgumentException> {
            JavaExpressionEvaluator("import java.lang.System; System.exit(1)", Void.TYPE)
        }
        println(exception.message)
    }

    @Test
    fun `shouldn't evaluate lambda expression`() {
        val exception = assertThrows<IllegalArgumentException> {
            JavaExpressionEvaluator(""" " ".chars().forEach( n -> { while(true); }) """, Void.TYPE)
        }
        println(exception.message)
    }

    @Test
    fun `shouldn't initialize array`() {
        val exception = assertThrows<IllegalArgumentException> {
            JavaExpressionEvaluator("""new double[1_000_000_000].toString()""", Void.TYPE)
        }
        println(exception.message)
    }

    @Test
    fun `shouldn't process private classes`() {
        val exception = assertThrows<CompileException> {
            JavaExpressionEvaluator(
                expression = """object.getActive()""",
                resultType = Void.TYPE,
                argumentTypes = mapOf("object" to PrivateObject::class.java)
            )
        }
        println(exception.message)
    }

    @Test
    fun `shouldn't process private properties`() {
        val exception = assertThrows<CompileException> {
            JavaExpressionEvaluator(
                expression = """object.getActive()""",
                resultType = Void.TYPE,
                argumentTypes = mapOf("object" to PrivatePropertyObject::class.java)
            )
        }
        println(exception.message)
    }

    @Test
    fun `should throw compile exception`() {
        val exception = assertThrows<CompileException> { JavaExpressionEvaluator("", Void.TYPE) }
        println(exception.stackTraceToString())
    }

    @Test
    fun `should throw evaluating exception`() {
        val evaluator = JavaExpressionEvaluator("0 / 0", Integer::class.java)
        val exception = assertThrows<InvocationTargetException> { evaluator.eval() }
        println(exception.stackTraceToString())
    }

    @Disabled
    @Test
    fun `measure evaluation time`() {
        val evaluationCount = 1_000_000
        val evaluator = JavaExpressionEvaluator(defaultExpression, defaultResultType, defaultImports, defaultArgumentTypes)
        val timedValue = measureTimedValue<Unit> {
            (1..evaluationCount).asSequence().forEach { evaluator.eval(defaultPerson) }
        }
        println("average evaluation time: ${timedValue.duration/evaluationCount}")
    }


    internal data class Person(
        val id: UUID,
        val name: String,
        val age: Int,
        val birthDate: ZonedDateTime,
        val employed: Boolean
    )

    private data class PrivateObject(
        val active: Boolean,
    )

    internal data class PrivatePropertyObject(
         private val active: Boolean,
    )
}