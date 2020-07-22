package io.github.oxisto.cppparser.ast

import io.github.oxisto.reticulated.grammar.CPP14Parser

abstract class Declaration : Node() {

}

class TranslationUnitDeclaration() : Declaration() {

}

abstract class NamedDeclaration(var name: DeclarationName? = null) : Declaration() {

}

abstract class ValueDeclaration(var type: Type? = null) : NamedDeclaration() {

}

abstract class TypedDeclaration(name: DeclarationName) : NamedDeclaration(name) {

    abstract fun toType(): Type?

}

class FunctionDeclaration() : ValueDeclaration() {

}

class VariableDeclaration() : ValueDeclaration() {

}

enum class ClassKey {
    CLASS,
    STRUCT,
    UNION
}

class RecordDeclaration(name: DeclarationName, key: ClassKey = ClassKey.CLASS) : TypedDeclaration(name) {

    override fun toType(): Type? {
        return name?.identifier?.let { Type(it) }
    }

}