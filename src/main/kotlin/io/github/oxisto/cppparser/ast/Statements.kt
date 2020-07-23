package io.github.oxisto.cppparser.ast

open class Statement : Node() {

}

class CompoundStatement : Statement() {

}

class DeclarationStatement(declarations: MutableList<Declaration>) : Statement() {

    var declarations: MutableList<Declaration> = declarations
        private set

    init {
        addChildren(declarations)
    }

}

class ReturnStatement(val expression: Expression?) : Statement() {

}

class Expression() : Statement() {}