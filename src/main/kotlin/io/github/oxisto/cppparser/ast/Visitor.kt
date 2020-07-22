package io.github.oxisto.cppparser.ast

import io.github.oxisto.reticulated.grammar.CPP14BaseVisitor
import io.github.oxisto.reticulated.grammar.CPP14Parser

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

    // parse the declaration specifiers for the type
    type = ctx.declspecifierseq().accept(TypeVisitor())

    // TODO: parse declaration specifiers for additional declarations

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
    val name = DeclarationName(ctx.text)

    return name
  }
}

class TypeVisitor : CPP14BaseVisitor<Type>() {
  override fun visitSimpletypespecifier(ctx: CPP14Parser.SimpletypespecifierContext): Type {
    // TODO: proper type parsing
    return Type(ctx.text.toString())
  }
}
