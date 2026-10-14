package com.example.tutorial

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tutorial.ui.theme.TutorialTheme

private val AzulPrincipal = Color(0xFF1565C0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TutorialApp()
        }
    }
}

// La app y el Preview utilizan la misma pantalla.
@Composable
private fun TutorialApp() {
    TutorialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFFF4F8FF)
        ) { innerPadding ->
            Greeting(
                name = "Roger",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Al cambiar este estado, Compose actualiza el contador en pantalla.
    // rememberSaveable conserva el valor al girar el teléfono.
    var contador by rememberSaveable { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Column coloca el saludo, el contador y los botones uno debajo del otro.
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Proyecto Práctico TIC",
                color = Color(0xFF42526B),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Hello $name!",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulPrincipal, RoundedCornerShape(50))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Clics: $contador",
                color = AzulPrincipal,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            // Botón 1: muestra un mensaje breve en el dispositivo.
            Button(
                onClick = {
                    Toast.makeText(context, "¡Hola, $name!", Toast.LENGTH_SHORT).show()
                    Log.d("Tutorial", "Botón Saludar presionado")
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulPrincipal,
                    contentColor = Color.White
                )
            ) {
                Text("Saludar", fontSize = 18.sp)
            }

            // Botón 2: suma un clic y muestra el nuevo total.
            Button(
                onClick = {
                    contador += 1
                    Log.d("Tutorial", "Contador: $contador")
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDCEBFF),
                    contentColor = AzulPrincipal
                )
            ) {
                Text("Sumar un clic", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun GreetingPreview() {
    TutorialApp()
}