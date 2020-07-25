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

  fun <N : NamedDeclaration> registerDeclaration(declaration: Redeclarable<N>): Boolean {
    if (declaration !is NamedDeclaration) return false

    val name = declaration.name ?: return false
    val previous = declarations.getOrDefault(name, null)

    if (previous != null) {
      declaration.setPreviousDeclaration(previous as Redeclarable<N>)
      logger.debug { "Re-declaring name '$name' of type ${declaration.nodeType}" }
    } else {
      declaration.setPreviousDeclaration(null)
      logger.debug { "Declaring name '$name' of type ${declaration.nodeType}" }
    }

    declarations[name] = declaration
    return true
  }

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
