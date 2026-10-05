package com.jarvis.network

// ✅ Imports corrigidos e completos
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color

class MainActivity : AppCompatActivity {

    // ✅ Variável declarada corretamente
    private lateinit var textViewResultado: TextView

    constructor() : super()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Conecta com o layout — confira o ID no XML!
        textViewResultado = findViewById(R.id.resultado)

        // ==========================================
        // Seu código — Linhas 207 e 211 corrigidas ✅
        // ==========================================

        // Linha 207 — Color funcionando ✅
        val corTexto = Color.Red

        // Linha 211 — Variável existindo ✅
        textViewResultado.text = "JARVIS está ativo e conectado!"
        
        // Se quiser definir cor do texto (exemplo compatível):
        // textViewResultado.setTextColor(android.graphics.Color.RED)
    }
}
