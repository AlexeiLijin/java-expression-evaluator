package alexeilijin.evaluation.expression.janino

/**
 * Adds imports to the expression
 */
internal object JavaExpressionPrepCook {

    internal fun prepare(expression: String, imports: List<String>): String {
        val importsStatement = imports.joinToString(separator = "\n", postfix = "\n").trimStart('\n')
        return "$importsStatement$expression"
    }
}