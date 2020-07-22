/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.github.oxisto.cppparser

import io.github.oxisto.cppparser.ast.TranslationUnitDeclaration
import io.github.oxisto.cppparser.ast.Visitor
import io.github.oxisto.reticulated.grammar.CPP14Lexer
import io.github.oxisto.reticulated.grammar.CPP14Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import java.nio.file.Path

class Parser {
    fun parse(path: Path): TranslationUnitDeclaration {
        val inputStream = CharStreams.fromPath(path)
        val lexer = CPP14Lexer(inputStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = CPP14Parser(tokenStream)

        val ctx = parser.translationunit()

        explore(ctx, 0)

        //val listener = TranslationUnitListener()
        //ParseTreeWalker.DEFAULT.walk(listener, ctx)
        return ctx.accept(Visitor()) as TranslationUnitDeclaration

        //return listener.tu
    }

    fun explore(node: ParseTree, indent: Int) {
        println("%s%s -> %s".format(" ".repeat(indent), node.javaClass.simpleName, node.text))

        if (node is ParserRuleContext) {
            node.children?.let {
                for (child in it) {
                    explore(child, indent + 2)
                }
            }
        }
    }
}
