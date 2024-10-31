package com.hari.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Stack

class MainActivity : AppCompatActivity() {
    private var resultTextView: TextView? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // result text
        resultTextView = findViewById(R.id.resultTextView)

        // number buttons

        val button0: Button = findViewById(R.id.button0)
        val button1: Button = findViewById(R.id.button1)
        val button2: Button = findViewById(R.id.button2)
        val button3: Button = findViewById(R.id.button3)
        val button4: Button = findViewById(R.id.button4)
        val button5: Button = findViewById(R.id.button5)
        val button6: Button = findViewById(R.id.button6)
        val button7: Button = findViewById(R.id.button7)
        val button8: Button = findViewById(R.id.button8)
        val button9: Button = findViewById(R.id.button9)

        // add listeners

        button0.setOnClickListener { appendText("0") }
        button1.setOnClickListener { appendText("1") }
        button2.setOnClickListener { appendText("2") }
        button3.setOnClickListener { appendText("3") }
        button4.setOnClickListener { appendText("4") }
        button5.setOnClickListener { appendText("5") }
        button6.setOnClickListener { appendText("6") }
        button7.setOnClickListener { appendText("7") }
        button8.setOnClickListener { appendText("8") }
        button9.setOnClickListener { appendText("9") }

        // operators

        val buttonPlus: Button = findViewById(R.id.button_plus)
        val buttonMinus: Button = findViewById(R.id.button_minus)
        val buttonDivide: Button = findViewById(R.id.button_divide)
        val buttonStar: Button = findViewById(R.id.button_star)
        val buttonEqual: Button = findViewById(R.id.button_equal)

        // add listeners

        buttonPlus.setOnClickListener { appendText("+") }
        buttonMinus.setOnClickListener { appendText("-") }
        buttonDivide.setOnClickListener { appendText("/") }
        buttonStar.setOnClickListener { appendText("*") }

        // clear button

        val buttonClear: Button = findViewById(R.id.button_clear)

        // add listeners

        buttonClear.setOnClickListener { resultTextView?.text = "" }


        // equal button

        buttonEqual.setOnClickListener {
            evaluate()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun evaluate() {
        if (resultTextView == null || resultTextView?.text == "") {
            resultTextView?.text = ""
            return;
        }
        try {
            val tokens = tokenize(resultTextView?.text.toString())
            val postfix = infixToPostfix(tokens)
            val result = evaluatePostfix(postfix)
            resultTextView?.text = formatResult(result)
        } catch (e: ArithmeticException) {
            resultTextView?.text = "Infinity"
        } catch (e: IllegalArgumentException) {
            resultTextView?.text = "Invalid Expression"
        }


    }


    fun formatResult(result: Double): String {
        return if (result.toString().length > 15) {
            // Use scientific notation with 8 significant figures
            String.format("%.8e", result)
        } else {
            // Limit to a maximum of 10 decimal places to stay within 15 characters
            String.format("%.10f", result).trimEnd('0').trimEnd('.')
        }
    }

    fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        val regex = Regex("""\d+(\.\d+)?|[-+*/()]""")
        regex.findAll(expression).forEach { match ->
            tokens.add(match.value)
        }
        return tokens
    }

    fun infixToPostfix(tokens: List<String>): List<String> {
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)
        val output = mutableListOf<String>()
        val operators = Stack<String>()

        for (token in tokens) {
            when {
                token.isNumber() -> output.add(token)
                token in precedence.keys -> {
                    while (operators.isNotEmpty() && operators.peek() != "(" &&
                        precedence[token]!! <= precedence[operators.peek()]!!
                    ) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }

                token == "(" -> operators.push(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        output.add(operators.pop())
                    }
                    if (operators.isEmpty()) throw IllegalArgumentException()
                    operators.pop()
                }
            }
        }

        while (operators.isNotEmpty()) {
            val op = operators.pop()
            if (op == "(") throw IllegalArgumentException()
            output.add(op)
        }

        return output
    }

    fun evaluatePostfix(postfix: List<String>): Double {
        val stack = Stack<Double>()

        for (token in postfix) {
            when {
                token.isNumber() -> stack.push(token.toDouble())
                else -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(
                        when (token) {
                            "+" -> a + b
                            "-" -> a - b
                            "*" -> a * b
                            "/" -> {
                                if (b == 0.0) throw ArithmeticException()
                                a / b
                            }

                            else -> throw IllegalArgumentException()
                        }
                    )
                }
            }
        }

        if (stack.size != 1) throw IllegalArgumentException()
        return stack.pop()
    }

    fun String.isNumber() = this.toDoubleOrNull() != null

    @SuppressLint("SetTextI18n")
    private fun appendText(text: String) {
        if (resultTextView != null && resultTextView!!.text.length >= 15) return;
        resultTextView?.text = resultTextView?.text.toString() + text
    }
}