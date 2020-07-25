package io.github.oxisto.cppparser.ast

import java.util.*

class PointerUnion()

interface Redeclarable<N : NamedDeclaration> : Iterable<N> {
  var first: Redeclarable<N>?
  var prev: Redeclarable<N>?
  var latest: Redeclarable<N>?

  override fun iterator(): Iterator<N> {
    return iterator(this.get())
  }

  fun get(): N {
    return this as N
  }

  fun iterator(node: N): Iterator<N> {
    class DeclarableIterator(node: N) : Iterator<N> {

      var current: N = node
      val starter: N = node
      var passedFirst: Boolean = false

      override fun hasNext(): Boolean {
        if (current === first) {
          if (passedFirst) {
            return false;
          }
          passedFirst = true
        }

        val next = current.getNextRedeclaration()
        return next !== current
      }

      override fun next(): N {
        val next = current.getNextRedeclaration()
        current = next as N

        return next
      }
    }

    return DeclarableIterator(node)
  }

  /**
   * Sets the previous declaration of this, if it exists. If it does not exist, this is probably the first.
   */
  fun setPreviousDeclaration(previousDeclaration: Redeclarable<N>?) {
    if (previousDeclaration != null) {
      // learn first from previous
      first = previousDeclaration.first
      assert(first != null)

      prev = previousDeclaration
    } else {
      first = this
    }

    // point first one to latest
    first?.latest = this

    // TODO: what about the others?!
  }

  fun getPrevious(): Redeclarable<N>? {
    // we do not know the latest
    if (latest == null) {
      // if we know the previous, return it
      if (prev != null) {
        return prev
      }
    }

    return latest
  }
}

abstract class Declaration : Node() {

  open fun getNextRedeclaration(): Declaration {
    return this
  }
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

class FunctionDeclaration(var isDefinition: Boolean = false) : ValueDeclaration(), Redeclarable<FunctionDeclaration> {

  override var latest: Redeclarable<FunctionDeclaration>? = null
  override var prev: Redeclarable<FunctionDeclaration>? = null
  override var first: Redeclarable<FunctionDeclaration>? = null

  var parameters: MutableList<ParamVariableDeclaration> = mutableListOf()

  var body: Statement? = null
    set(value) {
      value?.let { addChild(it) }
      field = value
    }

  val signature: String?
    get() {
      return name?.identifier + "(" +
          parameters.map { it.type?.name }.joinToString(", ") +
          ")" +
          type?.name
    }

  val definition: FunctionDeclaration?
    get() {
      for (declaration in this) {
        if (declaration.isDefinition) {
          return declaration
        }
      }

      return null
    }

  override fun getNextRedeclaration(): FunctionDeclaration {
    getPrevious()?.let { return it.get() }

    return this
  }

  override fun toString(): String {
    return "FunctionDeclaration(name=$name, parameters=$parameters)"
  }


}

open class VariableDeclaration() : ValueDeclaration() {

}

class ParamVariableDeclaration : VariableDeclaration() {

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
