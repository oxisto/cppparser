package io.github.oxisto.cppparser.ast

import io.github.oxisto.reticulated.grammar.CPP14BaseVisitor
import io.github.oxisto.reticulated.grammar.CPP14Parser
import org.antlr.v4.runtime.tree.TerminalNode

class Visitor : CPP14BaseVisitor<Node>() {

    override fun visitTranslationunit(ctx: CPP14Parser.TranslationunitContext): Node {
        val tu = TranslationUnitDeclaration()

        ctx.declarationseq()?.accept(DeclarationsVisitor())?.let { tu.children.addAll(it) }

        return tu
    }

}

/**
 * Visits declarations, which might actually contain multiple declarations within it.
 */
class DeclarationsVisitor : CPP14BaseVisitor<List<Declaration>>() {

    /**
     * The current type of this list of declaration.
     */
    private var type: Type? = null

    override fun visitDeclarationseq(ctx: CPP14Parser.DeclarationseqContext?): List<Declaration> {
        val list = mutableListOf<Declaration>()

        ctx?.declarationseq()?.accept(this)?.let { list.addAll(it) }

        // one declaration parse node can still contain a list of multiple AST declarations
        ctx?.declaration()?.accept(this)?.let { list.addAll(it) }

        return list
    }

    override fun visitSimpledeclaration(ctx: CPP14Parser.SimpledeclarationContext): List<Declaration> {
        val list = mutableListOf<Declaration>()

        val declaration = ctx.declspecifierseq().accept(DeclarationVisitor())
        if (declaration != null) {
            list.add(declaration)

            if (declaration is TypedDeclaration) {
                type = declaration.toType()
            }
        } else {
            // parse the declaration specifiers for the type
            type = ctx.declspecifierseq().accept(TypeVisitor())
        }

        // gather declarations from init declarator list
        ctx.initdeclaratorlist()?.accept(this)?.let { list.addAll(it) }

        return list
    }

    override fun visitInitdeclaratorlist(ctx: CPP14Parser.InitdeclaratorlistContext): List<Declaration> {
        val list = mutableListOf<Declaration>()

        ctx.initdeclaratorlist()?.accept(this)?.let { list.addAll(it) }
        ctx.initdeclarator()?.accept(DeclarationVisitor(type))?.let { list.add(it) }

        return list
    }
}

class DeclarationVisitor(
        /**
         * The current type of this declaration.
         */
        private val type: Type? = null) : CPP14BaseVisitor<Declaration>() {

    override fun visitDeclarator(ctx: CPP14Parser.DeclaratorContext): Declaration {
        return super.visitDeclarator(ctx)
    }

    override fun visitNoptrdeclarator(ctx: CPP14Parser.NoptrdeclaratorContext): NamedDeclaration {
        var declaration: NamedDeclaration

        if (ctx.parametersandqualifiers() != null && ctx.noptrdeclarator() != null) {
            declaration = ctx.accept(ValueDeclarationVisitor(FunctionDeclaration::class.java, type))
        } else {
            declaration = ctx.accept(ValueDeclarationVisitor(VariableDeclaration::class.java, type))
        }

        // TODO: array

        return declaration
    }

    override fun visitClassspecifier(ctx: CPP14Parser.ClassspecifierContext): Declaration {
        var record = ctx.classhead().accept(this) as RecordDeclaration

        var name = ctx

        return record
    }

    override fun visitClasshead(ctx: CPP14Parser.ClassheadContext): RecordDeclaration {
        val name = ctx.classheadname().accept(NameVisitor())
        val key: ClassKey = ClassKey.CLASS

        assert(ctx.classkey() != null)
        ctx.classkey().accept(ClassKeyVisitor())

        var record = RecordDeclaration(name, key)

        return record
    }

    override fun visitElaboratedtypespecifier(ctx: CPP14Parser.ElaboratedtypespecifierContext): RecordDeclaration {
        var name: DeclarationName = DeclarationName("")
        val key: ClassKey = ClassKey.CLASS

        if (ctx.Identifier() != null) {
            name = ctx.Identifier().accept(NameVisitor())
        }

        ctx.classkey()?.let { it.accept(ClassKeyVisitor()) }

        var record = RecordDeclaration(name, key)

        return record
    }

}

class ClassKeyVisitor : CPP14BaseVisitor<ClassKey>() {
    override fun visitTerminal(node: TerminalNode): ClassKey {
        return when (node.text) {
            "struct" -> ClassKey.STRUCT
            "union" -> ClassKey.UNION
            else -> ClassKey.CLASS
        }
    }
}

class ValueDeclarationVisitor<N : ValueDeclaration>(private val clazz: Class<N>,
                                                    /**
                                                     * The current type of this declaration.
                                                     */
                                                    private val type: Type? = null) : CPP14BaseVisitor<N>() {
    override fun visitNoptrdeclarator(ctx: CPP14Parser.NoptrdeclaratorContext?): N {
        val declaration = clazz.getDeclaredConstructor().newInstance()

        declaration.name = ctx?.declaratorid()?.accept(NameVisitor())
        declaration.type = type

        return declaration
    }

    override fun visitPtrdeclarator(ctx: CPP14Parser.PtrdeclaratorContext): N {
        if (ctx.ptroperator() != null) {
            val declaration = super.visitPtrdeclarator(ctx)

            declaration.type?.let { Type(it.name + " " + ctx.ptroperator().text) }
        }

        return super.visitPtrdeclarator(ctx)
    }
}

class NameVisitor : CPP14BaseVisitor<DeclarationName>() {
    override fun visitDeclaratorid(ctx: CPP14Parser.DeclaratoridContext): DeclarationName {
        return super.visitDeclaratorid(ctx)
    }

    override fun visitClassname(ctx: CPP14Parser.ClassnameContext): DeclarationName {
        return super.visitClassname(ctx)
    }

    override fun visitTerminal(node: TerminalNode): DeclarationName {
        val name = DeclarationName(node.text)

        return name
    }
}

class TypeVisitor : CPP14BaseVisitor<Type>() {
    override fun visitSimpletypespecifier(ctx: CPP14Parser.SimpletypespecifierContext): Type {
        // TODO: proper type parsing
        return Type(ctx.text.toString())
    }
}
