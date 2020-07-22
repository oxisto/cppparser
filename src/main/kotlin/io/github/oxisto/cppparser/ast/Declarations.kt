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

class FunctionDeclaration() : ValueDeclaration() {

}

class VariableDeclaration() : ValueDeclaration() {

}
