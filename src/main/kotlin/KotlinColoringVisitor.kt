package com.main

import generated.kotlinParser.KotlinLexer
import org.antlr.v4.runtime.CommonTokenStream


class KotlinColoringVisitor(
    private val tokenClassifications: MutableMap<Int, String>,
    private val tokens: CommonTokenStream
) {

    @Suppress("UNUSED_PARAMETER")
    fun visit(tree: Any?) {
        tokens.seek(0)
        tokens.fill()

        tokens.tokens.forEach { token ->
            tokenClassifications.putIfAbsent(token.tokenIndex, classifyToken(token.type))
        }
    }

    private fun classifyToken(tokenType: Int): String {
        return when (tokenType) {
            // Comments
            KotlinLexer.DelimitedComment, KotlinLexer.LineComment -> "comment"

            // Keywords - control flow (purple/magenta)
            KotlinLexer.IF, KotlinLexer.ELSE, KotlinLexer.WHEN, KotlinLexer.FOR,
            KotlinLexer.WHILE, KotlinLexer.DO, KotlinLexer.TRY, KotlinLexer.CATCH,
            KotlinLexer.FINALLY, KotlinLexer.THROW, KotlinLexer.RETURN,
            KotlinLexer.CONTINUE, KotlinLexer.BREAK -> "control-flow"

            // Keywords - declarations (orange)
            KotlinLexer.PACKAGE, KotlinLexer.IMPORT, KotlinLexer.CLASS,
            KotlinLexer.INTERFACE, KotlinLexer.FUN, KotlinLexer.OBJECT,
            KotlinLexer.VAL, KotlinLexer.VAR, KotlinLexer.TYPE_ALIAS,
            KotlinLexer.CONSTRUCTOR, KotlinLexer.INIT -> "declaration"

            // Keywords - reference (blue)
            KotlinLexer.THIS, KotlinLexer.SUPER, KotlinLexer.THIS_AT,
            KotlinLexer.SUPER_AT -> "reference"

            // Keywords - type operators (cyan)
            KotlinLexer.AS, KotlinLexer.IS, KotlinLexer.IN,
            KotlinLexer.NOT_IS, KotlinLexer.NOT_IN, KotlinLexer.AS_SAFE,
            KotlinLexer.TYPEOF -> "type-operator"

            // Keywords - other (orange)
            KotlinLexer.BY, KotlinLexer.COMPANION, KotlinLexer.WHERE,
            KotlinLexer.OUT, KotlinLexer.DYNAMIC, KotlinLexer.GET, KotlinLexer.SET -> "keyword"

            // Keywords with @ suffix
            KotlinLexer.RETURN_AT, KotlinLexer.CONTINUE_AT, KotlinLexer.BREAK_AT -> "control-flow"

            // Annotation targets (light purple)
            KotlinLexer.FILE, KotlinLexer.FIELD, KotlinLexer.PROPERTY,
            KotlinLexer.RECEIVER, KotlinLexer.PARAM, KotlinLexer.SETPARAM,
            KotlinLexer.DELEGATE -> "annotation-target"

            // Modifiers - visibility (yellow/gold)
            KotlinLexer.PUBLIC, KotlinLexer.PRIVATE, KotlinLexer.PROTECTED,
            KotlinLexer.INTERNAL -> "visibility"

            // Modifiers - class/interface (bright orange)
            KotlinLexer.ENUM, KotlinLexer.SEALED, KotlinLexer.ANNOTATION,
            KotlinLexer.DATA, KotlinLexer.INNER, KotlinLexer.VALUE -> "class-modifier"

            // Modifiers - function/property (light orange)
            KotlinLexer.TAILREC, KotlinLexer.OPERATOR, KotlinLexer.INLINE,
            KotlinLexer.INFIX, KotlinLexer.EXTERNAL, KotlinLexer.SUSPEND,
            KotlinLexer.OVERRIDE, KotlinLexer.ABSTRACT, KotlinLexer.FINAL,
            KotlinLexer.OPEN, KotlinLexer.CONST, KotlinLexer.LATEINIT -> "function-modifier"

            // Modifiers - parameter (light blue)
            KotlinLexer.VARARG, KotlinLexer.NOINLINE, KotlinLexer.CROSSINLINE,
            KotlinLexer.REIFIED -> "parameter-modifier"

            // Modifiers - multiplatform (pink)
            KotlinLexer.EXPECT, KotlinLexer.ACTUAL -> "platform-modifier"

            // Literals - boolean and null (light blue)
            KotlinLexer.BooleanLiteral, KotlinLexer.NullLiteral -> "literal"

            // Number literals
            KotlinLexer.IntegerLiteral, KotlinLexer.FloatLiteral,
            KotlinLexer.DoubleLiteral, KotlinLexer.RealLiteral,
            KotlinLexer.HexLiteral, KotlinLexer.BinLiteral,
            KotlinLexer.LongLiteral, KotlinLexer.UnsignedLiteral -> "number"

            // String literals
            KotlinLexer.CharacterLiteral, KotlinLexer.QUOTE_OPEN,
            KotlinLexer.QUOTE_CLOSE, KotlinLexer.TRIPLE_QUOTE_OPEN,
            KotlinLexer.TRIPLE_QUOTE_CLOSE, KotlinLexer.LineStrText,
            KotlinLexer.MultiLineStrText, KotlinLexer.LineStrRef,
            KotlinLexer.MultiLineStrRef, KotlinLexer.LineStrEscapedChar,
            KotlinLexer.MultiLineStringQuote -> "string"

            // Identifiers
            KotlinLexer.Identifier, KotlinLexer.IdentifierOrSoftKey,
            KotlinLexer.FieldIdentifier -> "identifier"

            // Brackets (bright yellow)
            KotlinLexer.LPAREN, KotlinLexer.RPAREN, KotlinLexer.LSQUARE,
            KotlinLexer.RSQUARE, KotlinLexer.LCURL, KotlinLexer.RCURL -> "bracket"

            // Punctuation (light gray)
            KotlinLexer.DOT, KotlinLexer.COMMA, KotlinLexer.COLON,
            KotlinLexer.SEMICOLON, KotlinLexer.COLONCOLON,
            KotlinLexer.DOUBLE_SEMICOLON -> "punctuation"

            // Operators - arithmetic (white)
            KotlinLexer.ADD, KotlinLexer.SUB, KotlinLexer.MULT,
            KotlinLexer.DIV, KotlinLexer.MOD -> "operator"

            // Operators - assignment (white)
            KotlinLexer.ASSIGNMENT, KotlinLexer.ADD_ASSIGNMENT,
            KotlinLexer.SUB_ASSIGNMENT, KotlinLexer.MULT_ASSIGNMENT,
            KotlinLexer.DIV_ASSIGNMENT, KotlinLexer.MOD_ASSIGNMENT -> "operator"

            // Operators - comparison (white)
            KotlinLexer.EQEQ, KotlinLexer.EQEQEQ, KotlinLexer.EXCL_EQ,
            KotlinLexer.EXCL_EQEQ, KotlinLexer.LANGLE, KotlinLexer.LE,
            KotlinLexer.RANGLE, KotlinLexer.GE -> "operator"

            // Operators - logical (white)
            KotlinLexer.CONJ, KotlinLexer.DISJ, KotlinLexer.EXCL_NO_WS,
            KotlinLexer.EXCL_WS -> "operator"

            // Operators - other (white)
            KotlinLexer.ARROW, KotlinLexer.DOUBLE_ARROW, KotlinLexer.RANGE,
            KotlinLexer.RANGE_UNTIL, KotlinLexer.INCR, KotlinLexer.DECR,
            KotlinLexer.QUEST_NO_WS, KotlinLexer.QUEST_WS,
            KotlinLexer.AMP -> "operator"

            // Annotations (yellow/gold)
            KotlinLexer.AT_NO_WS, KotlinLexer.AT_POST_WS,
            KotlinLexer.AT_PRE_WS, KotlinLexer.AT_BOTH_WS,
            KotlinLexer.HASH -> "annotation"

            // Single quote (for character literals)
            KotlinLexer.SINGLE_QUOTE -> "string"

            // Reserved
            KotlinLexer.RESERVED -> "keyword"

            // Shebang line (script header)
            KotlinLexer.ShebangLine -> "comment"

            // String expression starts
            KotlinLexer.LineStrExprStart, KotlinLexer.MultiLineStrExprStart -> "string"

            // Default for anything not explicitly classified
            else -> "default"
        }
    }
}

