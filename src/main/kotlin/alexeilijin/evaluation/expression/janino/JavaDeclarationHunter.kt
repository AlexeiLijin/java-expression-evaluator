package alexeilijin.evaluation.expression.janino

import org.codehaus.janino.Java
import org.codehaus.janino.Java.AbstractCompilationUnit
import org.codehaus.janino.Parser
import org.codehaus.janino.Scanner
import org.codehaus.janino.util.AbstractTraverser
import java.io.StringReader
import java.lang.RuntimeException

/**
 * Collects lambdas, new array declarations from an expression
 * https://github.com/janino-compiler/janino/blob/master/janino/src/main/java/org/codehaus/janino/samples/DeclarationCounter.java
 */
internal class JavaDeclarationHunter(private val code: String) {

    internal fun aport(): Declarations? {
        val codeReader = StringReader(code)
        var acu: AbstractCompilationUnit? = codeReader.use {
            Parser(Scanner(null, it)).parseAbstractCompilationUnit()
        }

        return acu?.let {
            traverser.visitAbstractCompilationUnit(acu)
            Declarations(
                lambdaDeclarations = traverser.lambdaDeclarations.toList(),
                arrayCreationDeclarations = traverser.arrayCreationDeclarations.toList(),
            )
        }
    }

    internal data class Declarations(
        val lambdaDeclarations: List<String>,
        val arrayCreationDeclarations: List<String>,
    )


    private val traverser = object : AbstractTraverser<RuntimeException>() {

        val lambdaDeclarations: MutableList<String> = mutableListOf()

        override fun traverseLambdaExpression(le: Java.LambdaExpression?) {
            le?.let { lambdaDeclarations.add(le.enclosingScope.toString()) }
            super.traverseLambdaExpression(le)
        }

        val arrayCreationDeclarations: MutableList<String> = mutableListOf()

        override fun traverseNewInitializedArray(nia: Java.NewInitializedArray?) {
            nia?.let { arrayCreationDeclarations.add(nia.toString()) }
            super.traverseNewInitializedArray(nia)
        }

        override fun traverseNewArray(na: Java.NewArray?) {
            na?.let { arrayCreationDeclarations.add(na.toString()) }
            super.traverseNewArray(na)
        }
    }
}
