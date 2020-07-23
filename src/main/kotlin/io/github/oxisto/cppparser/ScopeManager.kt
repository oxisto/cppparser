package io.github.oxisto.cppparser

import io.github.oxisto.cppparser.ast.TranslationUnitDeclaration
import io.github.oxisto.cppparser.ast.Type

class Scope {}

class GlobalScope {}

class ScopeManager {

    var currentTranslationUnit: TranslationUnitDeclaration? = null
    var currentPrefix = ""

    var types: MutableMap<String, Type> = mutableMapOf()

    fun qualifyId(unqualifiedId: String): String {
        return currentPrefix + unqualifiedId
    }

    fun addType(type: Type) {
        val qualifiedId = qualifyId(type.name)

        types[qualifiedId] = type
    }

    fun getType(unqualifiedId: String): Type? {
        val qualifiedId = qualifyId(unqualifiedId)

        return types[qualifiedId]
    }

}