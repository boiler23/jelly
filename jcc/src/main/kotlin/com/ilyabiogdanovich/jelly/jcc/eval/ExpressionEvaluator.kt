package com.ilyabiogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.jcc.JccParser
import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import com.ilyabogdanovich.jelly.utils.mapEitherRight
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Helper class to evaluate expressions into internal interpreter representation.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
class ExpressionEvaluator {
    /**
     * Does evaluation of the expression into [Var] instance.
     * @param evalContext current context to use.
     * @param parseContext given expression context, coming from the parser.
     * @return evaluated [Var] or null, if the evaluation fails.
     */
    fun evaluateExpression(
        evalContext: EvalContext,
        parseContext: JccParser.ExpressionContext
    ): Either<EvalError, Var> {
        return parseContext.ruleContext<JccParser.NumberContext>()?.number()
            ?: parseContext.ruleContext<JccParser.IdentifierContext>()?.id(evalContext)
            ?: parseContext.ruleContext<JccParser.SequenceContext>()?.seq(evalContext)
            ?: parseContext.cast<JccParser.PowerContext>()?.binary(evalContext)
            ?: parseContext.cast<JccParser.MuldivContext>()?.binary(evalContext)
            ?: parseContext.cast<JccParser.PlusminusContext>()?.binary(evalContext)
            ?: parseContext.cast<JccParser.UnaryContext>()?.unary(evalContext)
            ?: parseContext.cast<JccParser.ParenthesisContext>()?.parenthesis(evalContext)
            ?: parseContext.unsupported()
    }

    private inline fun <reified T : ParserRuleContext> JccParser.ExpressionContext.ruleContext() =
        getRuleContext(T::class.java, 0)

    private inline fun <reified T : ParserRuleContext> JccParser.ExpressionContext.cast() = this as? T

    private fun JccParser.NumberContext.number(): Either<EvalError, Var> {
        val intNum = text.toIntOrNull()
        return if (intNum != null) {
            Var.NumVar(Num.Integer(intNum)).asRight()
        } else {
            val realNum = text.toDoubleOrNull()
            if (realNum != null) {
                Var.NumVar(Num.Real(text.toDouble())).asRight()
            } else {
                toError(EvalError.Type.InvalidNumber).asLeft()
            }
        }
    }

    private fun JccParser.SequenceContext.seq(evalContext: EvalContext): Either<EvalError, Var> {
        return evaluateToInt(evalContext, expression(0)).mapEitherRight { start ->
            evaluateToInt(evalContext, expression(1)).mapRight { end ->
                Var.SeqVar(Seq(from = start, to = end))
            }
        }
    }

    private fun JccParser.IdentifierContext.id(evalContext: EvalContext): Either<EvalError, Var> =
        evalContext[text]?.asRight() ?: toError(EvalError.Type.UndeclaredVariable).asLeft()

    private fun JccParser.ParenthesisContext.parenthesis(evalContext: EvalContext): Either<EvalError, Var> =
        evaluateExpression(evalContext, expression())

    private fun JccParser.ExpressionContext.unsupported(): Either<EvalError, Var> =
        toError(EvalError.Type.UnsupportedExpression).asLeft()

    private fun JccParser.PowerContext.binary(evalContext: EvalContext) =
        binary(evalContext, POWER().text, expression(0), expression(1))

    private fun JccParser.MuldivContext.binary(evalContext: EvalContext) =
        binary(evalContext, MULDIV().text, expression(0), expression(1))

    private fun JccParser.PlusminusContext.binary(evalContext: EvalContext) =
        binary(evalContext, PLUSMINUS().text, expression(0), expression(1))

    private fun JccParser.ExpressionContext.binary(
        evalContext: EvalContext,
        operation: String,
        leftExpr: JccParser.ExpressionContext,
        rightExpr: JccParser.ExpressionContext
    ): Either<EvalError, Var> {
        return evaluateToNumber(evalContext, leftExpr).mapEitherRight { left ->
            evaluateToNumber(evalContext, rightExpr).mapEitherRight { right ->
                operate(operation, left, right)
            }
        }.mapRight { num -> Var.NumVar(num) }
    }

    private fun evaluateToNumber(
        evalContext: EvalContext,
        expr: JccParser.ExpressionContext
    ): Either<EvalError, Num> {
        return when (val evaluated = evaluateExpression(evalContext, expr)) {
            is Either.Right -> {
                when (val variable = evaluated.value) {
                    is Var.NumVar -> variable.v.asRight()
                    is Var.SeqVar -> return expr.toError(EvalError.Type.InvalidArithmeticOperand).asLeft()
                }
            }
            is Either.Left -> return evaluated.value.asLeft()
        }
    }

    private fun evaluateToInt(
        evalContext: EvalContext,
        expr: JccParser.ExpressionContext
    ): Either<EvalError, Int> {
        return when (val evaluated = evaluateToNumber(evalContext, expr)) {
            is Either.Right -> {
                when (val variable = evaluated.value) {
                    is Num.Integer -> variable.v.asRight()
                    is Num.Real -> return expr.toError(EvalError.Type.IntegerExpected).asLeft()
                }
            }
            is Either.Left -> return evaluated.value.asLeft()
        }
    }

    private fun JccParser.ExpressionContext.operate(operation: String, left: Num, right: Num): Either<EvalError, Num> {
        return when (operation) {
            "*" -> (left * right).asRight()
            "/" -> (left / right).asRight()
            "+" -> (left + right).asRight()
            "-" -> (left - right).asRight()
            "^" -> left.pow(right).asRight()
            else -> toError(EvalError.Type.InvalidArithmeticOperator).asLeft()
        }
    }

    private fun JccParser.UnaryContext.unary(evalContext: EvalContext) =
        unary(evalContext, PLUSMINUS().text, expression())

    private fun JccParser.ExpressionContext.unary(
        evalContext: EvalContext,
        operation: String,
        expr: JccParser.ExpressionContext
    ): Either<EvalError, Var> {
        return evaluateToNumber(evalContext, expr).mapEitherRight { value ->
            unaryOperate(operation, value)
        }.mapRight { num -> Var.NumVar(num) }
    }

private fun JccParser.ExpressionContext.unaryOperate(operation: String, num: Num): Either<EvalError, Num> {
    return when (operation) {
        "+" -> num.asRight()
        "-" -> (-num).asRight()
        else -> toError(EvalError.Type.InvalidArithmeticOperator).asLeft()
    }
}
}
