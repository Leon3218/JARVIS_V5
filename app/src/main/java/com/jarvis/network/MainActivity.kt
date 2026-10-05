package com.jarvis.network

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var networkInfo: TextView
    private lateinit var results: TextView
    private lateinit var timeDisplay: TextView
    private lateinit var dateDisplay: TextView
    private val worker = Executors.newFixedThreadPool(32)
    private val cyan = Color.rgb(69, 217, 255)
    private val bg = Color.rgb(7, 11, 20)
    private val card = Color.rgb(15, 24, 40)
    private var scanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildLauncherUi()
        requestNeededPermissions()
        refreshNetwork()
        updateDateTime()
    }

    private fun buildLauncherUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(40), dp(20), dp(20))
            setBackgroundColor(bg)
        }

        // 🕒 HORA E DATA
        timeDisplay = TextView(this).apply {
            text = "--:--"
            textSize = 52f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        dateDisplay = TextView(this).apply {
            text = "Carregando data…"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(30))
        }

        // 🤖 TÍTULO
        val title = TextView(this).apply {
            text = "J.A.R.V.I.S."
            textSize = 32f
            setTextColor(cyan)
            gravity = Gravity.CENTER
            letterSpacing = 0.2f
        }
        val subtitle = TextView(this).apply {
            text = "SISTEMA  •  ATIVO"
            textSize = 12f
            setTextColor(Color.rgb(150, 220, 255))
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(25))
        }

        // 📶 STATUS DE REDE
        status = TextView(this).apply {
            text = "● INICIALIZANDO"
            textSize = 15f
            setTextColor(cyan)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundColor(card)
        }

        networkInfo = TextView(this).apply {
            text = "Verificando conexão…"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(card)
        }

        // 🔘 BOTÕES
        val refreshBtn = makeButton("🔄 ATUALIZAR REDE") { refreshNetwork() }
        val scanBtn = makeButton("🔍 PROCURAR DISPOSITIVOS") { scanNetwork() }
        val voiceBtn = makeButton("🎙 COMANDO DE VOZ") { startVoice() }
        val appsBtn = makeButton("📂 APLICATIVOS") { openAppsList() }
        val settingsBtn = makeButton("⚙ CONFIGURAÇÕES") { openSystemSettings() }

        // 📋 ÁREA DE SAÍDA
        results = TextView(this).apply {
            text = "Sistema pronto.\nToque em um botão ou fale um comando."
            textSize = 14f
            setTextColor(Color.rgb(200, 220, 240))
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(card)
        }
        val scroll = ScrollView(this)
        scroll.addView(results)

        // MONTAGEM FINAL
        root.addView(timeDisplay, matchWrap())
        root.addView(dateDisplay, matchWrap())
        root.addView(title, matchWrap())
        root.addView(subtitle, matchWrap())
        root.addView(status, matchWrap())
        root.addView(networkInfo, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        root.addView(refreshBtn, matchWrap().apply { topMargin = dp(10) })
        root.addView(scanBtn, matchWrap().apply { topMargin = dp(8) })
        root.addView(voiceBtn, matchWrap().apply { topMargin = dp(8) })
        root.addView(appsBtn, matchWrap().apply { topMargin = dp(8) })
        root.addView(settingsBtn, matchWrap().apply { topMargin = dp(8) })
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(15) })

        setContentView(root)
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setTextColor(cyan)
        setBackgroundColor(card)
        isAllCaps = false
        textSize = 14f
        setPadding(dp(12), dp(14), dp(12), dp(14))
        setOnClickListener { action() }
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun updateDateTime() {
        val now = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(java.util.Date())
        val date = java.text.SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR")).format(java.util.Date())
        timeDisplay.text = now
        dateDisplay.text = date

        // Atualiza a cada minuto
        android.os.Handler(mainLooper).postDelayed({ updateDateTime() }, 60000)
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED
        ) needed.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.RECORD_AUDIO)
        
        if (Build.VERSION.SDK_INT <= 32 &&
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) needed.add(Manifest.permission.ACCESS_FINE_LOCATION)
        
        if (needed.isNotEmpty()) requestPermissions(needed.toTypedArray(), 41)
    }

    private fun refreshNetwork() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val active = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(active)
        val wifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = try { wm.dhcpInfo } catch (_: Exception) { null }
        val ip = dhcp?.ipAddress?.let { intIp(it) } ?: "indisponível"
        val gateway = dhcp?.gateway?.let { intIp(it) } ?: "indisponível"
        val mask = dhcp?.netmask?.let { intIp(it) } ?: "indisponível"

        status.text = if (wifi) "● WI-FI CONECTADO" else "● REDE MÓVEL / SEM CONEXÃO"
        networkInfo.text = """
            📡 Tipo: ${if (wifi) "Wi-Fi" else "Dados móveis"}
            🌐 IP: $ip
            📶 Gateway: $gateway
            📋 Máscara: $mask
        """.trimIndent()
    }

    private fun intIp(value: Int): String =
        "${value and 0xff}.${value shr 8 and 0xff}.${value shr 16 and 0xff}.${value shr 24 and 0xff}"

    private fun scanNetwork() {
        if (scanning) return

        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val dhcp = try { wm.dhcpInfo } catch (_: Exception) { null }
        val ip = dhcp?.ipAddress ?: 0
        val mask = dhcp?.netmask ?: 0

        if (ip == 0 || mask == 0) {
            results.text = "❌ Conecte ao Wi-Fi primeiro e toque em Atualizar Rede."
            return
        }

        val network = ip and mask
        val broadcast = network or mask.inv()
        val start = network + 1
        val end = broadcast - 1

        if (end - start > 1022) {
            results.text = "⚠️ Sub-rede muito grande para varredura rápida."
            return
        }

        scanning = true
        status.text = "● VARRENDO REDE…"
        results.text = "🔍 Verificando $start a $end…\nIsso pode levar até 30 segundos."

        Thread {
            val found = java.util.Collections.synchronizedList(mutableListOf<String>())
            val ports = listOf(80, 443, 554, 8008, 8080, 22, 445)

            val tasks = (start..end).filter { it != ip }.map { hostInt ->
                worker.submit {
                    val host = intIp(hostInt)
                    val open = mutableListOf<Int>()
                    for (port in ports) {
                        try {
                            Socket().use { s ->
                                s.connect(InetSocketAddress(host, port), 180)
                                open.add(port)
                            }
                        } catch (_: Exception) { }
                    }
                    if (open.isNotEmpty()) {
                        found.add("✅ $host  → portas: ${open.joinToString(", ")}")
                    }
                }
            }

            tasks.forEach {
                try { it.get(28, TimeUnit.SECONDS) }
                catch (_: Exception) { it.cancel(true) }
            }

            runOnUiThread {
                scanning = false
                status.text = "● CONCLUÍDO"
                results.text = if (found.isEmpty()) {
                    "Nenhum dispositivo detectado com portas abertas.\nDispositivos podem estar protegidos por firewall."
                } else {
                    "✅ ${found.size} dispositivo(s) encontrado(s):\n\n${found.sorted().joinToString("\n\n")}"
                }
            }
        }.start()
    }

    private fun startVoice() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestNeededPermissions()
            Toast.makeText(this, "Permita o microfone.", Toast.LENGTH_LONG).show()
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Reconhecimento de voz indisponível.", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale com o JARVIS")
        }
        startActivityForResult(intent, 42)
    }

    private fun openAppsList() {
        val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(mainIntent)
        Toast.makeText(this, "Escolha o launcher original para ver apps", Toast.LENGTH_SHORT).show()
    }

    private fun openSystemSettings() {
        startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            results.text = "🎤 Você disse: \"$spoken\"\n\n"
            val cmd = spoken.lowercase(Locale("pt", "BR"))
            when {
                "procur" in cmd || "dispositivo" in cmd -> scanNetwork()
                "rede" in cmd || "wi-fi" in cmd || "wifi" in cmd -> refreshNetwork()
                "configur" in cmd -> openSystemSettings()
                "aplicativo" in cmd || "app" in cmd -> openAppsList()
                else -> results.append("ℹ️ Comando recebido. Aguardando integração com IA.\nComandos disponíveis: procurar dispositivos, informações da rede, abrir configurações.")
            }
        }
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }
}
