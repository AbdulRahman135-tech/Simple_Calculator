package com.example.simple_calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simple_calculator.ui.theme.Simple_CalculatorTheme
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Simple_CalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var isShifted by remember { mutableStateOf(false) }

    fun onSymbolClick(symbol: String) {
        when (symbol) {
            "C" -> {
                expression = ""
                resultText = "0"
            }
            "=" -> {
                if (expression.isEmpty()) return
                try {
                    val result = evaluateExpression(expression)
                    if (result.isInfinite() || result.isNaN()) {
                        resultText = "Error"
                    } else {
                        // Use BigDecimal for formatting to avoid scientific notation ('E') 
                        // and ensure 10 significant figures.
                        val bd = BigDecimal(result, MathContext(10)).stripTrailingZeros()
                        resultText = bd.toPlainString()
                    }
                    expression = resultText
                } catch (e: Exception) {
                    resultText = "Error"
                }
            }
            "Shift" -> {
                isShifted = !isShifted
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "sin", "cos", "tan", "sinarc", "cosarc", "tanarc", "log" -> {
                expression += "$symbol("
            }
            else -> {
                expression += symbol
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Display area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expression.replace("sinarc", "sin⁻¹")
                        .replace("cosarc", "cos⁻¹")
                        .replace("tanarc", "tan⁻¹"),
                    fontSize = 24.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = resultText,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        val buttons = listOf(
            listOf("Shift", if (isShifted) "sinarc" else "sin", if (isShifted) "cosarc" else "cos", if (isShifted) "tanarc" else "tan"),
            listOf("log", "(", ")", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("C", "⌫", "0", ".", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { symbol ->
                    val isOperator = symbol in listOf("/", "*", "-", "+", "=")
                    val isFunction = symbol in listOf("sin", "cos", "tan", "sinarc", "cosarc", "tanarc", "log", "Shift")
                    val isClear = symbol in listOf("C", "⌫")
                    
                    Button(
                        onClick = { onSymbolClick(symbol) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(if (symbol == "=") 1f else 1.2f),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                symbol == "Shift" && isShifted -> MaterialTheme.colorScheme.tertiary
                                isOperator -> MaterialTheme.colorScheme.primary
                                isFunction -> MaterialTheme.colorScheme.secondary
                                isClear -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = when {
                                isOperator || isFunction || isClear -> MaterialTheme.colorScheme.onPrimary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    ) {
                        Text(
                            text = when (symbol) {
                                "Shift" -> "SHIFT"
                                "sinarc" -> "sin⁻¹"
                                "cosarc" -> "cos⁻¹"
                                "tanarc" -> "tan⁻¹"
                                else -> symbol
                            },
                            fontSize = if (symbol.length > 4) 14.sp else 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun evaluateExpression(expr: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < expr.length) expr[pos].toInt() else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.toInt()) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < expr.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.toInt())) x += parseTerm() // addition
                else if (eat('-'.toInt())) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.toInt())) x *= parseFactor() // multiplication
                else if (eat('/'.toInt())) x /= parseFactor() // division
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.toInt())) return parseFactor() // unary plus
            if (eat('-'.toInt())) return -parseFactor() // unary minus

            var x: Double
            val startPos = pos
            if (eat('('.toInt())) { // parentheses
                x = parseExpression()
                eat(')'.toInt())
            } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
                while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
                while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
                val func = expr.substring(startPos, pos)
                x = parseFactor()
                x = when (func) {
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "sinarc" -> Math.toDegrees(asin(x))
                    "cosarc" -> Math.toDegrees(acos(x))
                    "tanarc" -> Math.toDegrees(atan(x))
                    "log" -> log10(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            return x
        }
    }.parse()
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    Simple_CalculatorTheme {
        CalculatorScreen()
    }
}
