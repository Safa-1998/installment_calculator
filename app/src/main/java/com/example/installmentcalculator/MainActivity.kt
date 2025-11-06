package com.example.installmentcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.round
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

enum class Plan(val title: String) {
    P9_WITH_DOWN("9 месяцев"),
    P6_WITH_DOWN("6 месяцев"),
    P6_NO_DOWN("6 месяцев (без взноса)"),
    P3_WITH_DOWN("3 месяца"),
    P2_WITH_DOWN("2 месяца")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    MaterialTheme { Surface(Modifier.fillMaxSize()) { CalculatorScreen() } }
}

@Composable
fun CalculatorScreen() {
    var selectedPlan by remember { mutableStateOf(Plan.P9_WITH_DOWN) }
    var priceInput by remember { mutableStateOf("") }
    var resultMonthly by remember { mutableStateOf<String?>(null) }
    var resultDown by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf("") }

    fun roundToTens(x: Double): Double = round(x / 10.0) * 10.0
    fun fmt(label: String, value: Double): String =
        String.format(Locale.getDefault(), "%s: %.2f", label, value)

    fun compute() {
        errorText = ""
        resultMonthly = null
        resultDown = null
        val price = priceInput.replace(",", ".").toDoubleOrNull()
        if (price == null || price <= 0) {
            errorText = "Укажите корректную розничную цену (> 0)."
            return
        }

        val (down, monthly) = when (selectedPlan) {
            Plan.P9_WITH_DOWN -> {
                val newPrice = price * 1.15
                val down = newPrice * 0.25
                val base = (newPrice * 0.75) / 9.0
                down to (roundToTens(base) + 10.0)
            }
            Plan.P6_WITH_DOWN -> {
                val newPrice = price * 1.10
                val down = newPrice * 0.25
                val base = (newPrice * 0.75) / 6.0
                down to (roundToTens(base) + 50.0)
            }
            Plan.P3_WITH_DOWN -> {
                val newPrice = price * 1.05
                val down = newPrice * 0.25
                val base = (newPrice * 0.75) / 3.0
                down to (roundToTens(base) + 20.0)
            }
            Plan.P6_NO_DOWN -> 0.0 to (roundToTens((price * 1.20) / 6.0) + 50.0)
            Plan.P2_WITH_DOWN -> 0.0 to (price * 0.5)
        }

        resultDown = fmt("Взнос", down)
        resultMonthly = fmt("Ежемесячный платёж", monthly)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Название компании
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(

                text = "Mebel Mall",
                color = Color.Red,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.logo_mebel_mall),
                contentDescription = "Логотип",
                modifier = Modifier.size(56.dp) // при необходимости: 24..40dp
            )
        }


        Text("Калькулятор рассрочки", style = MaterialTheme.typography.titleMedium)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Выберите план:")
            Plan.entries.forEach { plan ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedPlan == plan,
                        onClick = { selectedPlan = plan },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color.Red,
                            unselectedColor = Color.Red
                        )
                    )
                    Text(plan.title, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        OutlinedTextField(
            value = priceInput,
            onValueChange = { priceInput = it },
            label = { Text("Розничная цена") },
            placeholder = { Text("например, 99990") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Red,
                cursorColor = Color.Red
            )
        )

        Button(
            onClick = { compute() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Text("Рассчитать")
        }

        if (errorText.isNotEmpty()) {
            Text(errorText, color = MaterialTheme.colorScheme.error)
        }

        resultDown?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        resultMonthly?.let { Text(it, style = MaterialTheme.typography.titleMedium) }

        AssistiveHint()
    }
}


@Composable
private fun AssistiveHint() {
    val lines = listOf(
        "9 мес: +15% к цене → взнос = 25% от новой цены; ежемес. = 75%/9, округл. до десятков +10",
        "6 мес (со взносом): +10% → взнос = 25%; ежемес. = 75%/6, округл. +50",
        "3 мес (со взносом): +5% → взнос = 25%; ежемес. = 75%/3, округл. +20",
        "6 мес (без взноса): (цена ×1.20)/6, округл. +50; взнос = 0",
        "2 мес: цена × 0.5 за месяц; взнос = 0"
    )
    Text(lines.joinToString("\n"), style = MaterialTheme.typography.bodySmall)
}
