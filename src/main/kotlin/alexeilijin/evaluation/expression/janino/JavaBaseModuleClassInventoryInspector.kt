package alexeilijin.evaluation.expression.janino

import java.lang.reflect.Modifier
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.nameWithoutExtension

/**
 * Lists visible classes from java.base module package
 */
object JavaBaseModuleClassInventoryInspector {

    private const val JAVA_CLASS_FILE_EXTENSION = "class"
    private const val JAVA_RUNTIME_FILE_SYSTEM_SCHEME = "jrt:/"
    private val jrtFileSystem = FileSystems.getFileSystem(
        URI.create(JAVA_RUNTIME_FILE_SYSTEM_SCHEME)
    ) ?: throw IllegalStateException("Can't get file system: $JAVA_RUNTIME_FILE_SYSTEM_SCHEME")
    private val javaBasePath = jrtFileSystem.getPath(
        "modules", "java.base"
    ) ?: throw IllegalStateException("Can't get path: modules/java.base")
    const val JAVA_PACKAGE_NAME_SEPARATOR = "."
    const val JAVA_INNER_CLASS_SEPARATOR = "$"
    const val JAVA_LANG_PACKAGE_NAME = "java.lang"

    fun listClasses(packageName: String = JAVA_LANG_PACKAGE_NAME): List<Class<*>> {
        val (javaLangPathHead, javaLangPathTail) = splitHeadAndTail(packageName, JAVA_PACKAGE_NAME_SEPARATOR)
        val javaLangPath = javaBasePath.resolve(
            jrtFileSystem.getPath(javaLangPathHead, *javaLangPathTail)
        ) ?: throw IllegalStateException("Can't get path: $packageName")

        val javaLangVisibleClasses = Files.walk(javaLangPath).filter {
            it?.isDirectory()?.not() == true
        }.filter {
            it.extension == JAVA_CLASS_FILE_EXTENSION
        }.map {
            javaBasePath.relativize(it) // make path relative
        }.map {
            it.map { it.nameWithoutExtension }
        }.map {
            it.joinToString(JAVA_PACKAGE_NAME_SEPARATOR)
        }.filter {
            it.contains(JAVA_INNER_CLASS_SEPARATOR).not() // get rid of inner classes
        }.map {
            Class.forName(it)
        }.filter {
            Modifier.isPrivate(it.modifiers).not() // get rid of private classes
        }.collect(Collectors.toList()) ?: throw IllegalStateException("Can't get files: $packageName")

        return javaLangVisibleClasses
    }

    private fun splitHeadAndTail(string: String, @Suppress("SameParameterValue") separator: String): Pair<String, Array<String>> {
        val split = string.split(separator)
        return split.first() to split.drop(1).toTypedArray()
    }
}