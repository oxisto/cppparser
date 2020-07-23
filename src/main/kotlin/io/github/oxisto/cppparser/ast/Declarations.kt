package io.github.oxisto.cppparser.ast

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

class FunctionDeclaration(var isDefinition: Boolean = false) : ValueDeclaration() {

    var body: Statement? = null
        set(value) {
            value?.let { addChild(it) }
            field = value
        }

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