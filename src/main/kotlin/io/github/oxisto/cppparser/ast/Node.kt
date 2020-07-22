package io.github.oxisto.cppparser.ast

open class Node(val children: MutableList<Node> = mutableListOf(), var parent: Node? = null) {

}

class Type(var name: String) {}

class Sequence<N : Node>() : Node() {
  val members: MutableList<N> = mutableListOf()
}
