package alexeilijin.evaluation.expression.janino

import org.codehaus.janino.ExpressionEvaluator

/**
 * A class for evaluating a Java expression with the specified result type, imports, and argument types.
 *
 * Only the expression is user-defined, imports and types are under the control of the developers.
 *
 * Validates and compiles the expression when instantiated using [Janino](https://janino-compiler.github.io/janino).
 *
 * To evaluate an expression, call evaluate(arguments).
 *
 * Only passed imports are allowed.
 *
 * The java.lang classes are prohibited, except for passed imports.
 *
 * Array declarations are prohibited.
 *
 * Java lambda expressions are prohibited.
 *
 * Arguments and their getters mustn't be private.
 * @param resultType Java expression result class, for void use Void.TYPE - in this case the result will be null
 * @param imports common Java imports such as: import java.lang.Object;
 * @param argumentTypes argument name -> argument type map
 * @throws [org.codehaus.commons.compiler.CompileException]
 */
class JavaExpressionEvaluator<O>(
    expression: String,
    resultType: Class<O>,
    imports: List<String> = emptyList(),
    argumentTypes: Map<String, Class<*>> = emptyMap()
) {

    // Dependencies
    private val prepCook = JavaExpressionPrepCook
    private val validator = JavaExpressionValidator

    private val evaluator = ExpressionEvaluator()

    init {
        val validation = validator.validate(expression, argumentTypes, imports)
        if (validation.isNullOrBlank()) {
            val argNames = argumentTypes.keys.toTypedArray()
            val argTypes = argumentTypes.values.toTypedArray()
            evaluator.setParameters(argNames, argTypes)
            evaluator.setExpressionType(resultType)
            val preparedExpression = prepCook.prepare(expression, imports)
            evaluator.cook(preparedExpression)
        } else {
            throw IllegalArgumentException(validation)
        }
    }

    /**
     * Evaluates the expression with given arguments and returns the result of the specified type.
     * @see [alexeilijin.evaluation.expression.janino.JavaExpressionEvaluator]
     * @throws [java.lang.reflect.InvocationTargetException]
     */
    fun eval(vararg arguments: Any): O {
        @Suppress("UNCHECKED_CAST")
        return (if (arguments.isEmpty()) evaluator.evaluate() else evaluator.evaluate(arguments)) as O
    }
}