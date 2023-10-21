package com.ilyabogdanovich.jelly.jcc.core.eval

import com.ilyabogdanovich.jelly.jcc.core.Error
import com.ilyabogdanovich.jelly.jcc.core.antlr.JccParser
import com.ilyabogdanovich.jelly.jcc.core.toError
import com.ilyabogdanovich.jelly.utils.Either
import com.ilyabogdanovich.jelly.utils.asLeft
import com.ilyabogdanovich.jelly.utils.asRight
import com.ilyabogdanovich.jelly.utils.mapEitherRight
import com.ilyabogdanovich.jelly.utils.mapRight
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Helper class to evaluate expressions into internal interpreter representation.
 *
 * @author Ilya Bogdanovich on 09.10.2023
 */
internal class ExpressionEvaluator {
    /**
     * Does evaluation of the expression into [Var] instance.
     * @param evalContext current context to use.
     * @param parseContext given expression context, coming from the parser.
     * @return evaluated [Var] or null, if the evaluation fails.
     */
    suspend fun evaluateExpression(
        evalContext: EvalContext,
        parseContext: JccParser.ExpressionContext
    ): Either<Error, Var> {
        if (!currentCoroutineContext().isActive) {
            throw CancellationException()
        }

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

    private fun JccParser.NumberContext.number(): Either<Error, Var> {
        val intNum = text.toLongOrNull()
        return if (intNum != null) {
            Var.NumVar(Num.Integer(intNum)).asRight()
        } else {
            val realNum = text.toDoubleOrNull()
            if (realNum != null) {
                Var.NumVar(Num.Real(text.toDouble())).asRight()
            } else {
                toError(Error.Type.InvalidNumber).asLeft()
            }
        }
    }

    private suspend fun JccParser.SequenceContext.seq(evalContext: EvalContext): Either<Error, Var> {
        val lower = expression(0) ?: return toError(Error.Type.MissingSequenceLowerBound).asLeft()
        val upper = expression(1) ?: return toError(Error.Type.MissingSequenceUpperBound).asLeft()

        return evaluateToInt(evalContext, lower, Error.Type.SequenceStartIsNotInteger).mapEitherRight { start ->
            evaluateToInt(evalContext, upper, Error.Type.SequenceStopIsNotInteger).mapEitherRight { end ->
                if (start <= end) {
                    val length = end - start
                    if (length <= MAX_SEQUENCE_LENGTH) {
                        Var.SeqVar(Seq.fromBounds(from = start, to = end)).asRight()
                    } else {
                        toError(Error.Type.SequenceTooLong, expression = "$length > $MAX_SEQUENCE_LENGTH").asLeft()
                    }
                } else {
                    toError(Error.Type.SequenceInvalidBounds, expression = "$start > $end").asLeft()
                }
            }
        }
    }

    private fun JccParser.IdentifierContext.id(evalContext: EvalContext): Either<Error, Var> =
        evalContext[text]?.asRight() ?: toError(Error.Type.UndeclaredVariable).asLeft()

    private suspend fun JccParser.ParenthesisContext.parenthesis(evalContext: EvalContext): Either<Error, Var> =
        evaluateExpression(evalContext, expression())

    private fun JccParser.ExpressionContext.unsupported(): Either<Error, Var> =
        toError(Error.Type.UnsupportedExpression).asLeft()

    private suspend fun JccParser.PowerContext.binary(evalContext: EvalContext): Either<Error, Var> {
        val operation = POWER()?.text ?: return toError(Error.Type.MissingOperator).asLeft()
        return binary(evalContext, operation, expression(0), expression(1))
    }

    private suspend fun JccParser.MuldivContext.binary(evalContext: EvalContext): Either<Error, Var> {
        val operation = MULDIV()?.text ?: return toError(Error.Type.MissingOperator).asLeft()
        return binary(evalContext, operation, expression(0), expression(1))
    }

    private suspend fun JccParser.PlusminusContext.binary(evalContext: EvalContext): Either<Error, Var> {
        val operation = PLUSMINUS()?.text ?: return toError(Error.Type.MissingOperator).asLeft()
        return binary(evalContext, operation, expression(0), expression(1))
    }

    private suspend fun JccParser.ExpressionContext.binary(
        evalContext: EvalContext,
        operation: String,
        leftExpr: JccParser.ExpressionContext?,
        rightExpr: JccParser.ExpressionContext?
    ): Either<Error, Var> {
        leftExpr ?: return toError(Error.Type.MissingLeftOperand).asLeft()
        rightExpr ?: return toError(Error.Type.MissingRightOperand).asLeft()

        return evaluateToNumber(evalContext, leftExpr, Error.Type.InvalidArithmeticOperand).mapEitherRight { left ->
            evaluateToNumber(evalContext, rightExpr, Error.Type.InvalidArithmeticOperand).mapEitherRight { right ->
                operate(operation, left, right)
            }
        }.mapRight { num -> Var.NumVar(num) }
    }

    private suspend fun evaluateToNumber(
        evalContext: EvalContext,
        expr: JccParser.ExpressionContext,
        errorType: Error.Type,
    ): Either<Error, Num> {
        return evaluateExpression(evalContext, expr).mapEitherRight { variable ->
            when (variable) {
                is Var.NumVar -> variable.v.asRight()
                is Var.SeqVar -> return@mapEitherRight expr.toError(errorType).asLeft()
            }
        }
    }

    private suspend fun evaluateToInt(
        evalContext: EvalContext,
        expr: JccParser.ExpressionContext,
        errorType: Error.Type,
    ): Either<Error, Long> {
        return evaluateToNumber(evalContext, expr, errorType).mapEitherRight { num ->
            when (num) {
                is Num.Integer -> num.v.asRight()
                is Num.Real -> return@mapEitherRight expr.toError(errorType).asLeft()
            }
        }
    }

    private suspend fun evaluateToSeq(
        evalContext: EvalContext,
        expr: JccParser.ExpressionContext
    ): Either<Error, Seq> {
        return evaluateExpression(evalContext, expr).mapEitherRight { variable ->
            when (variable) {
                is Var.NumVar -> return@mapEitherRight expr.toError(Error.Type.SequenceExpected).asLeft()
                is Var.SeqVar -> variable.v.asRight()
            }
        }
    }

    private fun JccParser.ExpressionContext.operate(operation: String, left: Num, right: Num): Either<Error, Num> {
        return when (operation) {
            "*" -> (left * right).asRight()
            "/" -> (left / right).asRight()
            "+" -> (left + right).asRight()
            "-" -> (left - right).asRight()
            "^" -> left.pow(right).asRight()
            else -> toError(Error.Type.InvalidArithmeticOperator).asLeft()
        }
    }

    private suspend fun JccParser.UnaryContext.unary(evalContext: EvalContext): Either<Error, Var> {
        val operation = PLUSMINUS()?.text ?: return toError(Error.Type.MissingOperator).asLeft()
        return unary(evalContext, operation, expression())
    }

    private suspend fun JccParser.ExpressionContext.unary(
        evalContext: EvalContext,
        operation: String,
        expr: JccParser.ExpressionContext?
    ): Either<Error, Var> {
        expr ?: return toError(Error.Type.MissingUnaryOperand).asLeft()
        return evaluateToNumber(evalContext, expr, Error.Type.InvalidArithmeticOperand).mapEitherRight { value ->
            unaryOperate(operation, value)
        }.mapRight { num -> Var.NumVar(num) }
    }

    private fun JccParser.ExpressionContext.unaryOperate(operation: String, num: Num): Either<Error, Num> {
        return when (operation) {
            "+" -> num.asRight()
            "-" -> (-num).asRight()
            else -> toError(Error.Type.InvalidArithmeticOperator).asLeft()
        }
    }

    private suspend fun JccParser.MapContext.map(evalContext: EvalContext): Either<Error, Var> {
        val seqExpr = expression() ?: return toError(Error.Type.MapMissingSequence).asLeft()
        val lambda = lambda1() ?: return toError(Error.Type.MapMissingLambda).asLeft()
        val id = lambda.identifier()?.NAME()?.text ?: return toError(Error.Type.MapMissingLambdaId).asLeft()
        val lambdaExpr = lambda.expression() ?: return toError(Error.Type.MapMissingLambdaExpression).asLeft()

        return evaluateToSeq(evalContext, seqExpr).mapEitherRight { seq ->
            val result = seq.parallelMap { e ->
                if (!currentCoroutineContext().isActive) {
                    throw CancellationException()
                }
                when (val localEvalContext = evalContext + mapOf(id to e.toVar())) {
                    is Either.Left -> lambdaExpr.toError(localEvalContext.value).asLeft()
                    is Either.Right ->
                        evaluateToNumber(localEvalContext.value, lambdaExpr, Error.Type.MapLambdaReturnsNotNumber)
                }
            }
            result.mapRight { Var.SeqVar(it) }
        }
    }

    private suspend fun JccParser.ReduceContext.reduce(evalContext: EvalContext): Either<Error, Var> {
        val seqExpr = expression(0) ?: return toError(Error.Type.ReduceMissingSequence).asLeft()
        val neutralExpr = expression(1) ?: return toError(Error.Type.ReduceMissingNeutral).asLeft()
        val lambda = lambda2() ?: return toError(Error.Type.ReduceMissingLambda).asLeft()
        val accumulatorId = lambda.identifier(0)?.NAME()?.text
            ?: return toError(Error.Type.ReduceMissingLambdaAccumulator).asLeft()
        val nextId = lambda.identifier(1)?.NAME()?.text
            ?: return toError(Error.Type.ReduceMissingLambdaNext).asLeft()
        val lambdaExpr = lambda.expression() ?: return toError(Error.Type.ReduceMissingLambdaExpression).asLeft()

        return evaluateToSeq(evalContext, seqExpr).mapEitherRight { seq ->
            evaluateToNumber(evalContext, neutralExpr, Error.Type.ReduceNeutralIsNotNumber)
                .mapEitherRight { neutral ->
                    val result = seq.parallelReduce(neutral) { accumulatorValue, nextValue ->
                        if (!currentCoroutineContext().isActive) {
                            throw CancellationException()
                        }
                        val localEvalContext = evalContext +
                            mapOf(accumulatorId to accumulatorValue.toVar(), nextId to nextValue.toVar())
                        when (localEvalContext) {
                            is Either.Left -> lambdaExpr.toError(localEvalContext.value).asLeft()
                            is Either.Right -> evaluateToNumber(
                                localEvalContext.value,
                                lambdaExpr,
                                Error.Type.ReduceLambdaReturnsNotNumber
                            )
                        }
                    }
                    result.mapRight { it.toVar() }
                }
        }
    }
}

// maximum supported sequence length
private const val MAX_SEQUENCE_LENGTH = 200_000_000
