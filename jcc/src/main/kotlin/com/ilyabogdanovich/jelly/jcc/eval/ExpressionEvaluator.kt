package com.ilyabogdanovich.jelly.jcc.eval

import com.ilyabogdanovich.jelly.jcc.JccParser
import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import com.ilyabogdanovich.jelly.utils.mapEitherRight
import com.ilyabogdanovich.jelly.utils.mapRight
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
    suspend fun evaluateExpression(
        evalContext: EvalContext,
        parseContext: JccParser.ExpressionContext
    ): Either<EvalError, Var> {
        return parseContext.ruleContext<JccParser.NumberContext>()?.number()
            ?: parseContext.ruleContext<JccParser.IdentifierContext>()?.id(evalContext)
            ?: parseContext.ruleContext<JccParser.SequenceContext>()?.seq(evalContext)
            ?: parseContext.ruleContext<JccParser.MapContext>()?.map(evalContext)
            ?: parseContext.ruleContext<JccParser.ReduceContext>()?.reduce(evalContext)
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

    private suspend fun JccParser.SequenceContext.seq(evalContext: EvalContext): Either<EvalError, Var> {
        val lower = expression(0) ?: return toError(EvalError.Type.MissingSequenceLowerBound).asLeft()
        val upper = expression(1) ?: return toError(EvalError.Type.MissingSequenceUpperBound).asLeft()

        return evaluateToInt(evalContext, lower).mapEitherRight { start ->
            evaluateToInt(evalContext, upper).mapRight { end ->
                Var.SeqVar(Seq.Bounds(from = start, to = end))
            }
        }
    }

    private fun JccParser.IdentifierContext.id(evalContext: EvalContext): Either<EvalError, Var> =
        evalContext[text]?.asRight() ?: toError(EvalError.Type.UndeclaredVariable).asLeft()

    private suspend fun JccParser.ParenthesisContext.parenthesis(evalContext: EvalContext): Either<EvalError, Var> =
        evaluateExpression(evalContext, expression())

    private fun JccParser.ExpressionContext.unsupported(): Either<EvalError, Var> =
        toError(EvalError.Type.UnsupportedExpression).asLeft()

    private suspend fun JccParser.PowerContext.binary(evalContext: EvalContext): Either<EvalError, Var> {
        val operation = POWER()?.text ?: return toError(EvalError.Type.MissingOperator).asLeft()
        return binary(evalContext, operation, expression(0), expression(1))
    }

    private suspend fun JccParser.MuldivContext.binary(evalContext: EvalContext): Either<EvalError, Var> {
        val operation = MULDIV()?.text ?: return toError(EvalError.Type.MissingOperator).asLeft()
        return binary(evalContext,operation, expression(0), expression(1))
    }

    private suspend fun JccParser.PlusminusContext.binary(evalContext: EvalContext): Either<EvalError, Var> {
        val operation = PLUSMINUS()?.text ?: return toError(EvalError.Type.MissingOperator).asLeft()
        return binary(evalContext, operation, expression(0), expression(1))
    }

    private suspend fun JccParser.ExpressionContext.binary(
        evalContext: EvalContext,
        operation: String,
        leftExpr: JccParser.ExpressionContext?,
        rightExpr: JccParser.ExpressionContext?
    ): Either<EvalError, Var> {
        leftExpr ?: return toError(EvalError.Type.MissingLeftOperand).asLeft()
        rightExpr ?: return toError(EvalError.Type.MissingRightOperand).asLeft()

        return evaluateToNumber(evalContext, leftExpr).mapEitherRight { left ->
            evaluateToNumber(evalContext, rightExpr).mapEitherRight { right ->
                operate(operation, left, right)
            }
        }.mapRight { num -> Var.NumVar(num) }
    }

    private suspend fun evaluateToNumber(
        evalContext: EvalContext,
        expr: JccParser.ExpressionContext
    ): Either<EvalError, Num> {
        return evaluateExpression(evalContext, expr).mapEitherRight { variable ->
            when (variable) {
                is Var.NumVar -> variable.v.asRight()
                is Var.SeqVar -> return@mapEitherRight expr.toError(EvalError.Type.InvalidArithmeticOperand).asLeft()
            }
        }
    }

    private suspend fun evaluateToInt(
        evalContext: EvalContext,
        expr: JccParser.ExpressionContext
    ): Either<EvalError, Int> {
        return evaluateToNumber(evalContext, expr).mapEitherRight { num ->
            when (num) {
                is Num.Integer -> num.v.asRight()
                is Num.Real -> return@mapEitherRight expr.toError(EvalError.Type.IntegerExpected).asLeft()
            }
        }
    }

    private suspend fun evaluateToSeq(
        evalContext: EvalContext,
        expr: JccParser.ExpressionContext
    ): Either<EvalError, Seq> {
        return evaluateExpression(evalContext, expr).mapEitherRight { variable ->
            when (variable) {
                is Var.NumVar -> return@mapEitherRight expr.toError(EvalError.Type.SequenceExpected).asLeft()
                is Var.SeqVar -> variable.v.asRight()
            }
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

    private suspend fun JccParser.UnaryContext.unary(evalContext: EvalContext): Either<EvalError, Var> {
        val operation = PLUSMINUS()?.text ?: return toError(EvalError.Type.MissingOperator).asLeft()
        return unary(evalContext, operation, expression())
    }

    private suspend fun JccParser.ExpressionContext.unary(
        evalContext: EvalContext,
        operation: String,
        expr: JccParser.ExpressionContext?
    ): Either<EvalError, Var> {
        expr ?: return toError(EvalError.Type.MissingUnaryOperand).asLeft()
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

    private suspend fun JccParser.MapContext.map(evalContext: EvalContext): Either<EvalError, Var> {
        val seqExpr = expression() ?: return toError(EvalError.Type.MapMissingSequence).asLeft()
        val lambda = lambda1() ?: return toError(EvalError.Type.MapMissingLambda).asLeft()
        val id = lambda.identifier()?.NAME()?.text ?: return toError(EvalError.Type.MapMissingLambdaId).asLeft()
        val lambdaExpr = lambda.expression() ?: return toError(EvalError.Type.MapMissingLambdaExpression).asLeft()

        return evaluateToSeq(evalContext, seqExpr).mapEitherRight { seq ->
            val result = seq.parallelMap { e ->
                when (val localEvalContext = evalContext + mapOf(id to e)) {
                    is Either.Left -> lambdaExpr.toError(localEvalContext.value).asLeft()
                    is Either.Right -> evaluateExpression(localEvalContext.value, lambdaExpr)
                }
            }
            result.mapRight { Var.SeqVar(it) }
        }
    }

    private suspend fun JccParser.ReduceContext.reduce(evalContext: EvalContext): Either<EvalError, Var> {
        val seqExpr = expression(0) ?: return toError(EvalError.Type.ReduceMissingSequence).asLeft()
        val neutralExpr = expression(1) ?: return toError(EvalError.Type.ReduceMissingNeutral).asLeft()
        val lambda = lambda2() ?: return toError(EvalError.Type.ReduceMissingLambda).asLeft()
        val accumulatorId = lambda.identifier(0)?.NAME()?.text
            ?: return toError(EvalError.Type.ReduceMissingLambdaAccumulator).asLeft()
        val nextId = lambda.identifier(1)?.NAME()?.text
            ?: return toError(EvalError.Type.ReduceMissingLambdaNext).asLeft()
        val lambdaExpr = lambda.expression() ?: return toError(EvalError.Type.ReduceMissingLambdaExpression).asLeft()

        return evaluateToSeq(evalContext, seqExpr).mapEitherRight { seq ->
            evaluateExpression(evalContext, neutralExpr).mapEitherRight { neutral ->
                val result = seq.parallelReduce(neutral) { accumulatorValue, nextValue ->
                    val localEvalContext = evalContext + mapOf(accumulatorId to accumulatorValue, nextId to nextValue)
                    when (localEvalContext) {
                        is Either.Left -> lambdaExpr.toError(localEvalContext.value).asLeft()
                        is Either.Right -> evaluateExpression(localEvalContext.value, lambdaExpr)
                    }
                }
                result
            }
        }
    }
}
