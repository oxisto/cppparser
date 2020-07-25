package io.github.oxisto.cppparser

import io.github.oxisto.cppparser.ast.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Scope {}

class GlobalScope {}

class ScopeManager {

  var currentTranslationUnit: TranslationUnitDeclaration? = null
  var currentPrefix = ""

  var types: MutableMap<String, Type> = mutableMapOf()

  // TODO: split into scopes
  var declarations: MutableMap<DeclarationName, NamedDeclaration> = mutableMapOf()

  fun qualifyId(unqualifiedId: String): String {
    return currentPrefix + unqualifiedId
  }

  /*inline fun <reified N : NamedDeclaration> registerDeclaration(declaration: N): Boolean {
    val name = declaration.name ?: return false
    var previous = declarations.getOrDefault(name, null)

    if (declaration is Redeclarable<*> && previous != null) {
      (declaration as Redeclarable<N>).setPreviousDeclaration(previous)
    } else {
      return false
    }

    logger.debug { "Registering name '$name' of type ${declaration.nodeType}" }

    if (declaration is Redeclarable<*>) {
      declaration.setPreviousDeclaration(null)
    }

    declarations[name] = declaration
    return true
  }*/

  inline fun <reified N : NamedDeclaration> getDeclaration(name: DeclarationName): N? {
    return declarations[name].takeIf { it is N } as N?
  }

  /**
   * @return true, if the type was newly registered. false, if it already exists
   */
  fun registerType(type: Type): Boolean {
    val qualifiedId = qualifyId(type.name)

    if (types.containsKey(qualifiedId)) {
      return false
    }

    logger.debug { "Registering type '${type.name}'" }

    types[qualifiedId] = type
    return true
  }

  fun getType(unqualifiedId: String): Type? {
    val qualifiedId = qualifyId(unqualifiedId)

    return types[qualifiedId]
  }


}
