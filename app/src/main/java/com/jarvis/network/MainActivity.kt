package com.jarvis.network

// ✅ IMPORTS CORRIGIDOS — NÃO REMOVA
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color

class MainActivity : AppCompatActivity {

    // ✅ Declaração correta da variável — use este nome no código!
    private lateinit var textViewResultado: TextView

    constructor() : super()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Conectando com o XML (certifique-se que o ID no XML é "resultado")
        textViewResultado = findViewById(R.id.resultado)

        // ==========================================
        // 🔹 SEU CÓDIGO VAI AQUI — EXEMPLO PRONTO:
        // ==========================================

        // Linha 207 — Exemplo de uso de Color (já importado ✅)
        val corTexto = Color.Red

        // Linha 211 — Uso correto da variável ✅
        textViewResultado.text = "JARVIS está ativo e conectado!"
        
        // Se quiser mudar a cor do texto (exemplo Compose):
        // textViewResultado.setTextColor(android.graphics.Color.RED)
    }
}
