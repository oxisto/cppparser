package io.github.oxisto.cppparser.ast

open class Node(val children: MutableList<Node> = mutableListOf(), var parent: Node? = null) {

    inline fun <reified N : NamedDeclaration> firstDeclaration(name: String): N? {
        return children.filter { it is N && it.name?.identifier == name }.map { it as N }.firstOrNull()
    }

}

open class Type(var name: String) {}

class RecordType(name: String, var declaration: RecordDeclaration?) : Type(name) {

}

class Sequence<N : Node>() : Node() {
    val members: MutableList<N> = mutableListOf()
}
