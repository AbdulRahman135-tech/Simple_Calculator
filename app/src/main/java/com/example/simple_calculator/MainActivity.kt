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
import androidx.compose.ui.graphics.Color
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
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    CalculatorScreen()
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
    var isRadian by remember { mutableStateOf(false) }
    var memory by remember { mutableStateOf(0.0) }

    fun onSymbolClick(symbol: String) {
        when (symbol) {
            "C" -> {
                expression = ""
                resultText = "0"
            }
            "=" -> {
                if (expression.isEmpty()) return
                try {
                    val result = evaluateExpression(expression, isRadian)
                    if (result.isInfinite() || result.isNaN()) {
                        resultText = "Error"
                    } else {
                        val bd = BigDecimal(result, MathContext(10)).stripTrailingZeros()
                        resultText = bd.toPlainString()
                    }
                    expression = resultText
                } catch (e: Exception) {
                    resultText = "Error"
                }
            }
            "Shift" -> isShifted = !isShifted
            "Rad/Deg" -> isRadian = !isRadian
            "⌫" -> if (expression.isNotEmpty()) expression = expression.dropLast(1)
            "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "abs" -> expression += "$symbol("
            "x²" -> expression += "^2"
            "10ˣ" -> expression += "10^"
            "eˣ" -> expression += "e^"
            "1/x" -> expression += "1/("
            "π" -> expression += "pi"
            "e" -> expression += "e"
            "x!" -> expression += "!"
            "M+" -> {
                try {
                    val res = evaluateExpression(resultText, isRadian)
                    memory += res
                } catch (e: Exception) {}
            }
            "M-" -> {
                try {
                    val res = evaluateExpression(resultText, isRadian)
                    memory -= res
                } catch (e: Exception) {}
            }
            "MR" -> {
                val mStr = BigDecimal(memory, MathContext(10)).stripTrailingZeros().toPlainString()
                expression += mStr
            }
            "MC" -> memory = 0.0
            else -> expression += symbol
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Display area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = if (isRadian) "RAD" else "DEG", color = Color(0xFFFF9F0A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (memory != 0.0) Text(text = "M", color = Color(0xFFFF9F0A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = expression.replace("asin", "sin⁻¹")
                        .replace("acos", "cos⁻¹")
                        .replace("atan", "tan⁻¹")
                        .replace("pi", "π")
                        .replace("sqrt", "√"),
                    fontSize = 18.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray,
                    maxLines = 2
                )
                Text(
                    text = resultText,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                )
            }
        }

        val buttons = listOf(
            listOf("Rad/Deg", "M+", "M-", "MR", "MC"),
            listOf("Shift", if (isShifted) "asin" else "sin", if (isShifted) "acos" else "cos", if (isShifted) "atan" else "tan", "ln"),
            listOf("log", "sqrt", "^", "(", ")"),
            listOf("π", "e", "x²", "x!", "abs"),
            listOf("1/x", "%", "⌫", "C", "/"),
            listOf("7", "8", "9", " ", "*"),
            listOf("4", "5", "6", "0", "-"),
            listOf("1", "2", "3", ".", "+"),
            listOf("=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { symbol ->
                    val isOperator = symbol in listOf("/", "*", "-", "+", "=", "^")
                    val isScientific = symbol in listOf("sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "Shift", "π", "e", "x²", "1/x", "sqrt", "(", ")", "%", "x!", "abs", "10ˣ", "eˣ", "Rad/Deg", "M+", "M-", "MR", "MC")
                    val isClear = symbol in listOf("C", "⌫")
                    
                    Button(
                        onClick = { onSymbolClick(symbol) },
                        modifier = Modifier
                            .weight(if (symbol == "=") 1f else 1f)
                            .aspectRatio(if (symbol == "=") 4f else 1.2f),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                symbol == "Shift" && isShifted -> Color(0xFFFF9F0A)
                                isOperator -> Color(0xFFFF9F0A)
                                isClear -> Color(0xFFFF3B30)
                                isScientific -> Color(0xFF3A3A3C)
                                else -> Color(0xFF636366)
                            },
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = when (symbol) {
                                "Shift" -> "SHIFT"
                                "asin" -> "sin⁻¹"
                                "acos" -> "cos⁻¹"
                                "atan" -> "tan⁻¹"
                                "sqrt" -> "√"
                                "Rad/Deg" -> if (isRadian) "RAD" else "DEG"
                                else -> symbol
                            },
                            fontSize = if (symbol.length > 4) 10.sp else 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // Fill remaining space for the rows if needed to keep column alignment
                if (row.size < 5 && row.first() != "=") {
                    repeat(5 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

fun evaluateExpression(expr: String, isRadian: Boolean): Double {
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
                if (eat('+'.toInt())) x += parseTerm()
                else if (eat('-'.toInt())) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.toInt())) x *= parseFactor()
                else if (eat('/'.toInt())) x /= parseFactor()
                else if (eat('%'.toInt())) x %= parseFactor()
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.toInt())) return parseFactor()
            if (eat('-'.toInt())) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.toInt())) {
                x = parseExpression()
                eat(')'.toInt())
            } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) {
                while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                x = expr.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) {
                while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
                val func = expr.substring(startPos, pos)
                if (func == "pi") return Math.PI
                if (func == "e") return Math.E
                
                x = parseFactor()
                x = when (func) {
                    "sin" -> if (isRadian) sin(x) else sin(Math.toRadians(x))
                    "cos" -> if (isRadian) cos(x) else cos(Math.toRadians(x))
                    "tan" -> if (isRadian) tan(x) else tan(Math.toRadians(x))
                    "asin" -> if (isRadian) asin(x) else Math.toDegrees(asin(x))
                    "acos" -> if (isRadian) acos(x) else Math.toDegrees(acos(x))
                    "atan" -> if (isRadian) atan(x) else Math.toDegrees(atan(x))
                    "log" -> log10(x)
                    "ln" -> ln(x)
                    "sqrt" -> sqrt(x)
                    "abs" -> abs(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            if (eat('^'.toInt())) x = x.pow(parseFactor())
            if (eat('!'.toInt())) x = factorial(x)

            return x
        }

        fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n)) return Double.NaN
            if (n > 170) return Double.POSITIVE_INFINITY // Limit for Double
            var res = 1.0
            for (i in 1..n.toInt()) res *= i
            return res
        }
    }.parse()
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    Simple_CalculatorTheme {
        Surface(color = Color.Black) {
            CalculatorScreen()
        }
    }
}
