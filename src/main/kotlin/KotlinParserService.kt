package com.main


import org.antlr.v4.runtime.*
import generated.kotlinParser.KotlinLexer
import generated.kotlinParser.KotlinParser


data class TokenInfo(
    val text: String,
    val type: String,
    val line: Int,
    val column: Int
)

data class ErrorInfo(
    val line: Int,
    val column: Int,
    val message: String
)

data class ParseResult(
    val tokens: List<TokenInfo>,
    val hasErrors: Boolean,
    val errors: List<String>,
    val errorLocations: List<ErrorInfo>
)

object KotlinParserService {
    private const val MAX_TOKENS = 10000

    fun parse(code: String): ParseResult {
        val errors = mutableListOf<String>()
        val errorLocations = mutableListOf<ErrorInfo>()
        val input = CharStreams.fromString(code)
        val lexer = KotlinLexer(input).apply {
            removeErrorListeners()
        }
        val tokens = CommonTokenStream(lexer)
        val parser = KotlinParser(tokens).apply {
            removeErrorListeners()
            addErrorListener(object : BaseErrorListener() {
                override fun syntaxError(
                    recognizer: Recognizer<*, *>?,
                    offendingSymbol: Any?,
                    line: Int,
                    charPositionInLine: Int,
                    msg: String?,
                    e: RecognitionException?
                ) {
                    val errorMsg = "Line $line:$charPositionInLine $msg"
                    errors.add(errorMsg)
                    errorLocations.add(ErrorInfo(line, charPositionInLine, msg ?: "Syntax error"))
                }
            })
        }

        val tree = try {
            parser.kotlinFile()
        } catch (_: Exception) {
            null
        }

        // Create a map of token positions to their semantic types using visitor
        val tokenClassifications = mutableMapOf<Int, String>()
        if (tree != null) {
            val visitor = KotlinColoringVisitor(tokenClassifications, tokens)
            visitor.visit(tree)
        }

        tokens.seek(0)
        tokens.fill()
        val collected = ArrayList<TokenInfo>(minOf(tokens.tokens.size, MAX_TOKENS))

        for (token in tokens.tokens) {
            if (token.type == Token.EOF) break

            val isComment = token.type == KotlinLexer.DelimitedComment ||
                           token.type == KotlinLexer.LineComment
            val isDefault = token.channel == Token.DEFAULT_CHANNEL

            if (!isComment && !isDefault) continue
            if (token.type == KotlinLexer.NL) continue

            val text = token.text ?: continue
            if (text.isEmpty()) continue

            // Use the semantic classification from visitor if available,
            // otherwise fall back to basic classification
            val classification = tokenClassifications[token.tokenIndex]
                ?: classifyBasic(token)

            collected.add(
                TokenInfo(
                    text = text,
                    type = classification,
                    line = token.line,
                    column = token.charPositionInLine
                )
            )

            if (collected.size >= MAX_TOKENS) break
        }

        return ParseResult(
            tokens = collected,
            hasErrors = errors.isNotEmpty(),
            errors = errors,
            errorLocations = errorLocations
        )
    }

    private fun classifyBasic(token: Token): String {
        return when (token.type) {
            KotlinLexer.DelimitedComment, KotlinLexer.LineComment -> "comment"

            // Keywords
            in setOf(
                KotlinLexer.PACKAGE, KotlinLexer.IMPORT, KotlinLexer.CLASS, KotlinLexer.INTERFACE,
                KotlinLexer.FUN, KotlinLexer.OBJECT, KotlinLexer.VAL, KotlinLexer.VAR,
                KotlinLexer.TYPE_ALIAS, KotlinLexer.CONSTRUCTOR, KotlinLexer.BY, KotlinLexer.COMPANION,
                KotlinLexer.INIT, KotlinLexer.THIS, KotlinLexer.SUPER, KotlinLexer.TYPEOF,
                KotlinLexer.WHERE, KotlinLexer.IF, KotlinLexer.ELSE, KotlinLexer.WHEN,
                KotlinLexer.TRY, KotlinLexer.CATCH, KotlinLexer.FINALLY, KotlinLexer.FOR,
                KotlinLexer.DO, KotlinLexer.WHILE, KotlinLexer.THROW, KotlinLexer.RETURN,
                KotlinLexer.CONTINUE, KotlinLexer.BREAK, KotlinLexer.AS, KotlinLexer.IS,
                KotlinLexer.IN, KotlinLexer.NOT_IS, KotlinLexer.NOT_IN, KotlinLexer.OUT,
                KotlinLexer.DYNAMIC, KotlinLexer.GET, KotlinLexer.SET,
                // Annotation targets
                KotlinLexer.FILE, KotlinLexer.FIELD, KotlinLexer.PROPERTY, KotlinLexer.RECEIVER,
                KotlinLexer.PARAM, KotlinLexer.SETPARAM, KotlinLexer.DELEGATE,
                // Labeled returns/breaks
                KotlinLexer.RETURN_AT, KotlinLexer.CONTINUE_AT, KotlinLexer.BREAK_AT,
                KotlinLexer.THIS_AT, KotlinLexer.SUPER_AT
            ) -> "keyword"

            // Literals
            in setOf(KotlinLexer.BooleanLiteral, KotlinLexer.NullLiteral) -> "keyword"

            // Modifiers
            in setOf(
                KotlinLexer.PUBLIC, KotlinLexer.PRIVATE, KotlinLexer.PROTECTED, KotlinLexer.INTERNAL,
                KotlinLexer.ENUM, KotlinLexer.SEALED, KotlinLexer.ANNOTATION, KotlinLexer.DATA,
                KotlinLexer.INNER, KotlinLexer.VALUE, KotlinLexer.TAILREC, KotlinLexer.OPERATOR,
                KotlinLexer.INLINE, KotlinLexer.INFIX, KotlinLexer.EXTERNAL, KotlinLexer.SUSPEND,
                KotlinLexer.OVERRIDE, KotlinLexer.ABSTRACT, KotlinLexer.FINAL, KotlinLexer.OPEN,
                KotlinLexer.CONST, KotlinLexer.LATEINIT, KotlinLexer.VARARG, KotlinLexer.NOINLINE,
                KotlinLexer.CROSSINLINE, KotlinLexer.REIFIED, KotlinLexer.EXPECT, KotlinLexer.ACTUAL
            ) -> "modifier"

            // Numbers
            in setOf(
                KotlinLexer.IntegerLiteral, KotlinLexer.FloatLiteral, KotlinLexer.DoubleLiteral,
                KotlinLexer.RealLiteral, KotlinLexer.HexLiteral, KotlinLexer.BinLiteral,
                KotlinLexer.LongLiteral, KotlinLexer.UnsignedLiteral
            ) -> "number"

            // Strings
            in setOf(
                KotlinLexer.CharacterLiteral, KotlinLexer.QUOTE_OPEN, KotlinLexer.QUOTE_CLOSE,
                KotlinLexer.TRIPLE_QUOTE_OPEN, KotlinLexer.TRIPLE_QUOTE_CLOSE,
                KotlinLexer.LineStrText, KotlinLexer.MultiLineStrText,
                KotlinLexer.LineStrRef, KotlinLexer.MultiLineStrRef,
                KotlinLexer.LineStrEscapedChar
            ) -> "string"

            // Identifiers
            in setOf(KotlinLexer.Identifier, KotlinLexer.IdentifierOrSoftKey) -> "identifier"

            // Brackets
            in setOf(
                KotlinLexer.LPAREN, KotlinLexer.RPAREN, KotlinLexer.LSQUARE, KotlinLexer.RSQUARE,
                KotlinLexer.LCURL, KotlinLexer.RCURL
            ) -> "bracket"

            // Punctuation
            in setOf(
                KotlinLexer.DOT, KotlinLexer.COMMA, KotlinLexer.COLON, KotlinLexer.SEMICOLON,
                KotlinLexer.COLONCOLON
            ) -> "punctuation"

            // Operators
            in setOf(
                KotlinLexer.ADD, KotlinLexer.SUB, KotlinLexer.MULT, KotlinLexer.DIV, KotlinLexer.MOD,
                KotlinLexer.ASSIGNMENT, KotlinLexer.EQEQ, KotlinLexer.EQEQEQ, KotlinLexer.EXCL_EQ,
                KotlinLexer.EXCL_EQEQ, KotlinLexer.LANGLE, KotlinLexer.LE, KotlinLexer.RANGLE,
                KotlinLexer.GE, KotlinLexer.ARROW, KotlinLexer.DOUBLE_ARROW, KotlinLexer.RANGE,
                KotlinLexer.RANGE_UNTIL, KotlinLexer.EXCL_NO_WS, KotlinLexer.EXCL_WS,
                KotlinLexer.QUEST_NO_WS, KotlinLexer.QUEST_WS, KotlinLexer.AMP, KotlinLexer.CONJ,
                KotlinLexer.DISJ, KotlinLexer.INCR, KotlinLexer.DECR, KotlinLexer.ADD_ASSIGNMENT,
                KotlinLexer.SUB_ASSIGNMENT, KotlinLexer.MULT_ASSIGNMENT, KotlinLexer.DIV_ASSIGNMENT,
                KotlinLexer.MOD_ASSIGNMENT, KotlinLexer.AS_SAFE
            ) -> "operator"

            // Annotations
            in setOf(
                KotlinLexer.AT_NO_WS, KotlinLexer.AT_POST_WS, KotlinLexer.AT_PRE_WS,
                KotlinLexer.AT_BOTH_WS, KotlinLexer.HASH
            ) -> "annotation"

            else -> "default"
        }
    }

    fun toJson(result: ParseResult): String {
        fun escapeJson(string: String) = buildString(string.length + 8) {
            string.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }

        val tokensJson = result.tokens.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        ) { token ->
            "{" +
                "\"text\":\"${escapeJson(token.text)}\"," +
                "\"type\":\"${escapeJson(token.type)}\"," +
                "\"line\":${token.line}," +
                "\"column\":${token.column}" +
            "}"
        }

        val errorsJson = result.errors.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        ) { error ->
            "\"${escapeJson(error)}\""
        }

        val errorLocationsJson = result.errorLocations.joinToString(
            separator = ",",
            prefix = "[",
            postfix = "]"
        ) { errorLoc ->
            "{" +
                "\"line\":${errorLoc.line}," +
                "\"column\":${errorLoc.column}," +
                "\"message\":\"${escapeJson(errorLoc.message)}\"" +
            "}"
        }

        return "{" +
            "\"tokens\":$tokensJson," +
            "\"hasErrors\":${result.hasErrors}," +
            "\"errors\":$errorsJson," +
            "\"errorLocations\":$errorLocationsJson" +
        "}"
    }
}

