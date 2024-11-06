package alexeilijin.evaluation.expression.janino

import java.util.UUID
import kotlin.collections.mapNotNull

/**
 * Validates Java expression:
 * - expression should be compilable
 * - imports are prohibited
 * - java.lang classes are prohibited except externally imported
 * - lambdas are prohibited
 * - array initializations are prohibited
 */
internal object JavaExpressionValidator {

    // Dependencies
    private val prepCook = JavaExpressionPrepCook
    private val implicitClassesInspector = JavaBaseModuleClassInventoryInspector

    private val spaceRegex = Regex("\\s")
    private val javaImportStatementRegex = Regex("\\s*import\\s+(?:static\\s+)?([^;]+);") // group[1]: java class
    private val javaImplicitClassFullyQualifiedNames =
        implicitClassesInspector.listClasses(implicitClassesInspector.JAVA_LANG_PACKAGE_NAME).map { it.name }

    internal fun validate(
        expression: String,
        argumentTypes: Map<String, Class<*>>,
        allowedImports: List<String>
    ): String? {
        val errorBuilder = StringBuilder()

        val (expressionImports, expressionBody) = splitToImportsAndBody(expression)
        if (expressionImports.isNotEmpty())
            errorBuilder.append("imports are prohibited, only these classes can be used: $allowedImports\n")

        val prohibitedClasses = findImplicitClasses(expression, allowedImports)
        if (prohibitedClasses.isNotEmpty())
            errorBuilder.append("implicit classes are prohibited: $prohibitedClasses\n")

        val javaApp = prepCook.prepare(wrapInJavaApp(expressionBody, argumentTypes), allowedImports)
        val declarations = JavaDeclarationHunter(javaApp).aport()
        if (declarations == null) {
            errorBuilder.append("can't compile expression\n")
        } else {
            if (declarations.lambdaDeclarations.isNotEmpty())
                errorBuilder.append("lambdas are prohibited: ${declarations.lambdaDeclarations}\n")
            if (declarations.arrayCreationDeclarations.isNotEmpty())
                errorBuilder.append("array declarations are prohibited: ${declarations.arrayCreationDeclarations}\n")
        }

        val errorMessage = errorBuilder.toString()
        return errorMessage.ifBlank { null }?.let { "Java expression compile errors:\n$errorMessage" }
    }


    private fun findImplicitClasses(expression: String, allowedImports: List<String>): List<String> {
        val importClassFullyQualifiedNames = allowedImports.mapNotNull {
            javaImportStatementRegex.find(it)?.groups[1]
        }.map {
            it.value.replace(spaceRegex, "")
        }
        val unwantedClassFullyQualifiedNames =
            javaImplicitClassFullyQualifiedNames.subtract(importClassFullyQualifiedNames)
        val unwantedClassSimpleNames = unwantedClassFullyQualifiedNames.map { it.split(".").last() }
        return unwantedClassSimpleNames.filter { Regex("\\b$it\\b").find(expression) != null }
    }

    private val mainClassName = "J${UUID.randomUUID().toString().replace("-", "")}"

    private fun wrapInJavaApp(expression: String, argumentTypes: Map<String, Class<*>>): String {
        val argumentStubs = argumentTypes.map { entry ->
            "${entry.value.name} ${entry.key} = (${entry.value.name}) new Object();"
        }.joinToString("\n")

        return """
            |public class $mainClassName {
            |    public static void main(String[] args) {}
            |    Boolean script() {
            |        $argumentStubs
            |        return ${expression.lines().map { it.trim() }.joinToString("\n\t\t")};
            |    }
            |}""".trimMargin("|")
    }

    private fun splitToImportsAndBody(expression: String): Pair<List<String>, String> {
        val imports = javaImportStatementRegex.findAll(expression).mapNotNull { it.groups[0]?.value }.toList()
        val body = imports.fold(expression) { expression, import -> expression.replace(import, "") }.trim()
        return imports to body
    }
}