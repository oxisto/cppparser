package io.github.oxisto.cppparser.ast

open class Node(var parent: Node? = null) {

    var children: MutableList<Node> = mutableListOf()
        private set

    inline fun <reified N : NamedDeclaration> firstDeclaration(name: String): N? {
        return children.filter { it is N && it.name?.identifier == name }.map { it as N }.firstOrNull()
    }

    fun addChild(node: Node) {
        node.parent = this
        children.add(node)
    }

    fun addChildren(nodes: List<Node>) {
        nodes.forEach { addChild(it) }
    }

}

open class Type(var name: String) {}

class RecordType(name: String, var declaration: RecordDeclaration?) : Type(name) {

}

class Sequence<N : Node>() : Node() {
    val members: MutableList<N> = mutableListOf()
}

