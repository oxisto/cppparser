package io.github.oxisto.cppparser.ast

class PointerUnion()

interface Redeclarable<N : NamedDeclaration> : Iterable<N> {
  var first: Redeclarable<N>?
  var prev: Redeclarable<N>?
  var latest: Redeclarable<N>?

  override fun iterator(): Iterator<N> {
    return iterator(this)
  }

  fun get(): N {
    return this as N
  }

  fun iterator(node: Redeclarable<N>): Iterator<N> {
    class DeclarableIterator(node: Redeclarable<N>) : Iterator<N> {

      var current: Redeclarable<N> = node
      val starter: Redeclarable<N> = node
      var passedFirst: Boolean = false

      override fun hasNext(): Boolean {
        if (current === first) {
          if (passedFirst) {
            return false;
          }
          passedFirst = true
        }

        val next = getNextDeclaration()
        return next !== starter
      }

      override fun next(): N {
        val next = current.getNextDeclaration()
        current = next!!

        return next.get()
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

  fun getNextDeclaration(): Redeclarable<N>? {
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

  var definition: FunctionDeclaration? = null

  val signature: String?
    get() {
      return name?.identifier + "(" +
          parameters.map { it.type?.name }.joinToString(", ") +
          ")" +
          type?.name
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
