package com.pratik.axon.features.calculator

import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import java.util.Locale
import com.pratik.axon.core.security.RiskLevel

class CalculatorTool : AxonTool {

    override fun getName(): String {
        return "Calculator"
    }

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        val hasMathOperator =
            text.contains("plus") ||
            text.contains("add") ||
            text.contains("added to") ||
            text.contains("minus") ||
            text.contains("subtract") ||
            text.contains("take away") ||
            text.contains("less") ||
            text.contains("times") ||
            text.contains("into") ||
            text.contains("multiply") ||
            text.contains("multiplied by") ||
            text.contains("divided by") ||
            text.contains("divide by") ||
            text.contains("over")

        val hasCalculateWord =
            text.contains("calculate")

        val hasMathQuestion =
            text.contains("what is") ||
            text.contains("what's") ||
            text.contains("how much is")

        val hasNumbers =
            text.any { it.isDigit() }

        return hasCalculateWord ||
                (hasMathOperator && hasNumbers) ||
                (hasMathQuestion && hasMathOperator)
    }

    override fun execute(command: String): AxonResult {

        var expression =
            command
                .lowercase(Locale.getDefault())

        expression =
            expression
                .replace("calculate", "")
                .replace("what is", "")
                .replace("what's", "")
                .replace("how much is", "")
                .replace("please", "")
                .trim()

        // =====================================================
        // ADDITION
        // =====================================================

        expression =
            expression
                .replace("added to", "+")
                .replace("plus", "+")
                .replace("add", "+")

        // =====================================================
        // SUBTRACTION
        // =====================================================

        expression =
            expression
                .replace("take away", "-")
                .replace("minus", "-")
                .replace("subtract", "-")
                .replace("less", "-")

        // =====================================================
        // MULTIPLICATION
        // =====================================================

        expression =
            expression
                .replace("multiplied by", "*")
                .replace("multiply by", "*")
                .replace("multiply", "*")
                .replace("times", "*")
                .replace("into", "*")

        // =====================================================
        // DIVISION
        // =====================================================

        expression =
            expression
                .replace("divided by", "/")
                .replace("divide by", "/")
                .replace("divide", "/")
                .replace("over", "/")

        // =====================================================
        // SYMBOL NORMALIZATION
        // =====================================================

        expression =
            expression
                .replace("×", "*")
                .replace("÷", "/")

        // =====================================================
        // EXTRACT BASIC EXPRESSION
        // =====================================================

        val regex =
            Regex(
                """(-?\d+(?:\.\d+)?)\s*([+\-*/])\s*(-?\d+(?:\.\d+)?)"""
            )

        val match =
            regex.find(expression)
                ?: return AxonResult(
                    success = false,
                    message = "I couldn't understand that calculation."
                )

        val number1 =
            match.groupValues[1].toDouble()

        val operator =
            match.groupValues[2]

        val number2 =
            match.groupValues[3].toDouble()

        // =====================================================
        // CALCULATE
        // =====================================================

        val result =
            when (operator) {

                "+" ->
                    number1 + number2

                "-" ->
                    number1 - number2

                "*" ->
                    number1 * number2

                "/" -> {

                    if (number2 == 0.0) {

                        return AxonResult(
                            success = false,
                            message = "I cannot divide by zero."
                        )
                    }

                    number1 / number2
                }

                else ->
                    return AxonResult(
                        success = false,
                        message = "I couldn't calculate that."
                    )
            }

        return AxonResult(
    success = true,
    message = "The answer is ${formatNumber(result)}. " +
        "Debug: I calculated $number1 $operator $number2."
)
    }

    private fun formatNumber(
        number: Double
    ): String {

        return if (
            number == number.toLong().toDouble()
        ) {

            number.toLong().toString()

        } else {

            number.toString()
        }
    }
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.SAFE
}
}