package io.github.oxisto.cppparser.ast

import io.github.oxisto.cppparser.ScopeManager
import io.github.oxisto.cppparser.expectNotNull
import io.github.oxisto.cppparser.expectTrue
import io.github.oxisto.reticulated.grammar.CPP14BaseVisitor
import io.github.oxisto.reticulated.grammar.CPP14Parser
import org.antlr.v4.runtime.tree.TerminalNode

class Visitor(var scope: ScopeManager) : CPP14BaseVisitor<Node>() {

    override fun visitTranslationunit(ctx: CPP14Parser.TranslationunitContext): Node {
        val tu = TranslationUnitDeclaration()

        ctx.declarationseq()?.accept(DeclarationsVisitor(scope))?.let { tu.addChildren(it) }

        return tu
    }

}

/**
 * Visits declarations, which might actually contain multiple declarations within it.
 */
class DeclarationsVisitor(var scope: ScopeManager) : CPP14BaseVisitor<MutableList<Declaration>>() {

    /**
     * The current type of this list of declaration.
     */
    private var type: Type? = null

    override fun visitDeclarationseq(ctx: CPP14Parser.DeclarationseqContext?): MutableList<Declaration> {
        val list = mutableListOf<Declaration>()

        ctx?.declarationseq()?.accept(this)?.let { list.addAll(it) }

        // one declaration parse node can still contain a list of multiple AST declarations
        ctx?.declaration()?.accept(this)?.let { list.addAll(it) }

        return list
    }

    override fun visitSimpledeclaration(ctx: CPP14Parser.SimpledeclarationContext): MutableList<Declaration> {
        val list = mutableListOf<Declaration>()

        // parse the declaration specifiers for the type
        ctx.declspecifierseq().accept(TypeVisitor(scope)).let {
            type = it

            // check for additional declarations because of the type
            gatherDeclarationsFromType(it, list)
        }

        // gather declarations from init declarator list
        ctx.initdeclaratorlist()?.accept(this)?.let { list.addAll(it) }

        return list
    }

    override fun visitFunctiondefinition(ctx: CPP14Parser.FunctiondefinitionContext): MutableList<Declaration> {
        val list = mutableListOf<Declaration>()

        // parse the declaration specifiers for the type
        ctx.declspecifierseq().accept(TypeVisitor(scope)).let {
            type = it

            // check for additional declarations because of the type
            gatherDeclarationsFromType(it, list)
        }

        ctx.declarator().accept(DeclarationVisitor(scope, type)).let {
            if (it is FunctionDeclaration) {
                it.isDefinition = true

                expectNotNull(ctx.functionbody(), "Expecting function body")

                val statement = ctx.functionbody().accept(StatementVisitor(scope))

                it.body = statement
            }

            list.add(it)
        }

        return list
    }

    override fun visitInitdeclaratorlist(ctx: CPP14Parser.InitdeclaratorlistContext): MutableList<Declaration> {
        val list = mutableListOf<Declaration>()

        ctx.initdeclaratorlist()?.accept(this)?.let { list.addAll(it) }
        ctx.initdeclarator()?.accept(DeclarationVisitor(scope, type))?.let { list.add(it) }

        return list
    }
}

class DeclarationVisitor(
        var scope: ScopeManager,
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
            declaration = ctx.noptrdeclarator().accept(ValueDeclarationVisitor(scope, FunctionDeclaration::class.java, type))
        } else {
            declaration = ctx.accept(ValueDeclarationVisitor(scope, VariableDeclaration::class.java, type))
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
        val name = ctx.classheadname().accept(NameVisitor(scope))
        var key: ClassKey = ClassKey.CLASS

        expectNotNull(ctx.classkey(), "Expecting class key")
        ctx.classkey().accept(ClassKeyVisitor(scope))?.let { key = it }

        var record = RecordDeclaration(name, key)

        return record
    }

}

class StatementsVisitor(var scope: ScopeManager) : CPP14BaseVisitor<MutableList<Statement>>() {

    override fun visitStatementseq(ctx: CPP14Parser.StatementseqContext?): MutableList<Statement> {
        val list = mutableListOf<Statement>()

        ctx?.statementseq()?.accept(this)?.let { list.addAll(it) }
        ctx?.statement()?.accept(StatementVisitor(scope))?.let { list.add(it) }

        return list
    }

}

class StatementVisitor(var scope: ScopeManager) : CPP14BaseVisitor<Statement>() {
    override fun visitCompoundstatement(ctx: CPP14Parser.CompoundstatementContext?): CompoundStatement {
        val compoundStatement = CompoundStatement()

        ctx?.statementseq()?.accept(StatementsVisitor(scope))?.let { compoundStatement.addChildren(it) }

        return compoundStatement
    }

    override fun visitDeclarationstatement(ctx: CPP14Parser.DeclarationstatementContext): Statement {
        expectNotNull(ctx.blockdeclaration(), "Expected block declaration")

        val declarations = ctx.blockdeclaration().accept(DeclarationsVisitor(scope))

        val statement = DeclarationStatement(declarations)

        return statement
    }

    override fun visitJumpstatement(ctx: CPP14Parser.JumpstatementContext): Statement {
        if (ctx.Return() != null) {
            // TODO: braced initializer list

            var expression: Expression?
            ctx.expression()?.accept(ExpressionVisitor(scope)).let { expression = it }

            val returnStatement = ReturnStatement(expression)

            return returnStatement
        }

        TODO()
    }
}

class ExpressionVisitor(var scope: ScopeManager) : CPP14BaseVisitor<Expression>() {

}

class ClassKeyVisitor(var scope: ScopeManager) : CPP14BaseVisitor<ClassKey>() {
    override fun visitTerminal(node: TerminalNode): ClassKey {
        return when (node.text) {
            "struct" -> ClassKey.STRUCT
            "union" -> ClassKey.UNION
            else -> ClassKey.CLASS
        }
    }
}

class ValueDeclarationVisitor<N : ValueDeclaration>(var scope: ScopeManager, private val clazz: Class<N>,
                                                    /**
                                                     * The current type of this declaration.
                                                     */
                                                    private val type: Type? = null) : CPP14BaseVisitor<N>() {
    override fun visitNoptrdeclarator(ctx: CPP14Parser.NoptrdeclaratorContext?): N {
        val declaration = clazz.getDeclaredConstructor().newInstance()

        declaration.name = ctx?.declaratorid()?.accept(NameVisitor(scope))
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

class NameVisitor(var scope: ScopeManager) : CPP14BaseVisitor<DeclarationName>() {
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

class TypeVisitor(var scope: ScopeManager) : CPP14BaseVisitor<Type>() {
    override fun visitSimpletypespecifier(ctx: CPP14Parser.SimpletypespecifierContext): Type {
        // TODO: proper type parsing
        return Type(ctx.text.toString())
    }

    override fun visitClassspecifier(ctx: CPP14Parser.ClassspecifierContext): Type {
        val declaration = ctx.accept(DeclarationVisitor(scope))

        val name = ctx.classhead().accept(NameVisitor(scope))

        // create new record type
        val type = RecordType(name.identifier, declaration as RecordDeclaration)

        return type
    }


    override fun visitElaboratedtypespecifier(ctx: CPP14Parser.ElaboratedtypespecifierContext): Type {
        var name = DeclarationName("")
        var key: ClassKey = ClassKey.CLASS
        var type: Type?

        if (ctx.Identifier() != null) {
            name = ctx.Identifier().accept(NameVisitor(scope))
        }

        // look for existing type
        type = scope.getType(name.identifier)

        if (type != null) {
            return type
        }

        ctx.classkey()?.accept(ClassKeyVisitor(scope))?.let { key = it }

        val declaration = RecordDeclaration(name, key)

        // create new record type
        type = RecordType(name.identifier, declaration)

        return type
    }
}

fun gatherDeclarationsFromType(type: Type, list: MutableList<Declaration>) {
    if (type is RecordType) {
        type.declaration?.let { list.add(it) }
    }
}
