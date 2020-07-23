package io.github.oxisto.cppparser.ast

import io.github.oxisto.cppparser.expectTrue

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

open class Type(var name: String) {

}

class RecordType(name: String, var declaration: RecordDeclaration?) : Type(name) {

}

class BuiltInType(kind: Kind) : Type(kind.internal) {

    var kind = kind
        set(value) {
            name = value.internal
            field = value
        }

    enum class Kind(var internal: String) {
        Void("void"),
        Char("char"),
        Char16("char16_t"),
        Char32("char32_t"),
        SignedChar("signed char"),
        Wchar("wchar_t"),
        Bool("bool"),
        Short("short"),
        Int("int"),
        Long("long"),
        LongLong("long long"),
        Float("float"),
        Double("double"),
        UnsignedChar("unsigned char"),
        UnsignedShort("unsigned short"),
        UnsignedInt("unsigned int"),
        UnsignedLong("unsigned long"),
        UnsignedLongLong("unsigned long long"),
        Auto("auto"),
    }

    fun sign() {
        when (kind) {
            Kind.Char -> kind = Kind.SignedChar
            Kind.Short -> kind = Kind.Short
            Kind.Int -> kind = Kind.Int
            Kind.Long -> kind = Kind.Long
            Kind.LongLong -> kind = Kind.LongLong
            else -> {
                expectTrue(false, "'%s' cannot be signed".format(kind.internal))
            }
        }
    }

    fun unsign() {
        when (kind) {
            Kind.Char -> kind = Kind.UnsignedChar
            Kind.Short -> kind = Kind.UnsignedShort
            Kind.Int -> kind = Kind.UnsignedInt
            Kind.Long -> kind = Kind.UnsignedLong
            Kind.LongLong -> kind = Kind.UnsignedLongLong
            else -> {
                expectTrue(false, "'%s' cannot be unsigned".format(kind.internal))
            }
        }
    }
}

class Sequence<N : Node>() : Node() {
    val members: MutableList<N> = mutableListOf()
}

