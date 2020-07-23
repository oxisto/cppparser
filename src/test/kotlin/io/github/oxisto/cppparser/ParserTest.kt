/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.github.oxisto.cppparser

import io.github.oxisto.cppparser.ast.*
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun testParse() {
        val parser = Parser()

        val tu = parser.parse(Paths.get("src", "test", "resources", "declarations.cpp"))
        assertNotNull(tu)
    }

    @Test
    fun testClass() {
        val parser = Parser()

        val tu = parser.parse(Paths.get("src", "test", "resources", "class.cpp"))
        assertNotNull(tu)

        val a = tu.firstDeclaration<VariableDeclaration>("a")
        assertNotNull(a)
        assertDeclaredName("a", a)

        val classA = tu.firstDeclaration<RecordDeclaration>("A")
        assertNotNull(classA)
        assertDeclaredName("A", classA)

        val classB = tu.firstDeclaration<RecordDeclaration>("B")
        assertNotNull(classB)
        assertDeclaredName("B", classB)

        val classC = tu.firstDeclaration<RecordDeclaration>("C")
        assertNotNull(classC)
        assertDeclaredName("C", classC)
    }

    @Test
    fun testCompoundStatement() {
        val parser = Parser()

        val tu = parser.parse(Paths.get("src", "test", "resources", "compoundstmt.cpp"))
        assertNotNull(tu)

        val someFunction = tu.firstDeclaration<FunctionDeclaration>("someFunction")
        assertNotNull(someFunction)

        val body = someFunction.body as CompoundStatement
        assertEquals(2, body.children.size)
    }
}


fun assertDeclaredName(identifier: String, node: NamedDeclaration) {
    assertEquals(DeclarationName(identifier), node.name)
}
