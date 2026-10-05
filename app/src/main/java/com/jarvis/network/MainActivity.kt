package com.jarvis.network

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity {

    private lateinit var textViewResultado: TextView

    constructor() : super()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewResultado = findViewById(R.id.resultado)

        textViewResultado.text = "JARVIS está ativo e conectado!"
        textViewResultado.setTextColor(android.graphics.Color.RED)
    }
}
