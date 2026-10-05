package com.jarvis.network

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JarvisApp()
        }
    }
}

@Composable
fun JarvisApp() {

    var command by remember {
        mutableStateOf("")
    }

    var response by remember {
        mutableStateOf(
            "Sistema JARVIS iniciado. Aguardando comando."
        )
    }

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF070B11)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "JARVIS",
                        color = Color(0xFF65D9FF),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "NETWORK ASSISTANT",
                        color = Color(0xFF9AA7B8),
                        fontSize = 13.sp
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(
                                Color(0xFF0D1622),
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier
                                    .height(110.dp)
                                    .fillMaxWidth(0.55f)
                                    .background(
                                        Color(0xFF102A3A),
                                        CircleShape
                                    ),
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Text(
                                    text = "J",
                                    color = Color(0xFF65D9FF),
                                    fontSize = 56.sp
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Text(
                                text = "ONLINE",
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Text(
                        text = response,
                        color = Color(0xFFE7EEF7),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }

                Column {

                    OutlinedTextField(
                        value = command,
                        onValueChange = {
                            command = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Digite um comando")
                        },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        Button(
                            onClick = {

                                response =
                                    if (command.isBlank()) {

                                        "Diga ou digite um comando."

                                    } else {

                                        "Comando recebido: $command"
                                    }

                                command = ""
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF0EA5E9)
                            )
                        ) {

                            Text("EXECUTAR")
                        }

                        Button(
                            onClick = {

                                response =
                                    "Modo de voz preparado para integração."

                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF172033)
                            )
                        ) {

                            Text("VOZ")
                        }
                    }
                }
            }
        }
    }
}
