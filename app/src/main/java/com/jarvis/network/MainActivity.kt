package com.jarvis.v5

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Núcleo de Inteligência Artificial Avançado para o JARVIS V6.
 * Suporta múltiplos provedores (OpenAI, Gemini, Anthropic), orquestração
 * de ações no Android e gestão dinâmica de memória/contexto.
 */
class AIClient(private val secrets: SecretStore) {
    private val pool = Executors.newCachedThreadPool()

    /**
     * Envia uma mensagem para o provedor de IA configurado de forma assíncrona.
     */
    fun ask(user: String, memory: List<String>, callback: (String) -> Unit) {
        pool.execute {
            val answer = try {
                route(user, memory)
            } catch (e: Exception) {
                "Falha no núcleo de IA: ${e.message ?: "erro desconhecido"}"
            }
            callback(answer)
        }
    }

    /**
     * Executa consultas estruturadas de Agente utilizando o AgentContext.
     */
    fun askAgent(context: AgentContext, callback: (String) -> Unit) {
        pool.execute {
            val fullMemory = context.rememberedFacts + context.recentConversation
            val answer = try {
                route(context.userMessage, fullMemory)
            } catch (e: Exception) {
                "Falha na execução do Agente JARVIS: ${e.message ?: "erro desconhecido"}"
            }
            callback(answer)
        }
    }

    /**
     * Roteia a requisição para o provedor configurado no SecretStore.
     */
    private fun route(user: String, memory: List<String>): String {
        val provider = secrets.get("provider")?.lowercase()
        return when (provider) {
            "gemini" -> callGemini(user, memory)
            "anthropic" -> callAnthropic(user, memory)
            "openai" -> callOpenAI(user, memory)
            else -> "A IA ainda não foi configurada. O núcleo local está funcional. Configure um provedor (OpenAI, Gemini ou Anthropic) e sua chave API nas configurações."
        }
    }

    /**
     * Prompt de sistema aprimorado com orquestração de ferramentas e regras do Android.
     */
    private fun prompt(user: String, memory: List<String>): String {
        val history = if (memory.isEmpty()) "(sem histórico de conversas ou fatos)" else memory.joinToString("\n")
        return """
Você é JARVIS, um assistente pessoal avançado em português do Brasil integrado nativamente ao Android.
Sua personalidade é extremamente eficiente, educada, precisa e prestativa.

REGRAS DE EXECUÇÃO DE FERRAMENTAS:
1. Nunca invente que executou uma ação no Android se o aplicativo não puder executá-la.
2. Quando a solicitação do usuário envolver controle do dispositivo, prefira emitir um comando de ferramenta.
3. Ferramentas Android disponíveis: battery, flashlight, camera, settings, wifi, bluetooth, notifications, sound, location, home, search, url, app.

FORMATO DE RESPOSTA PARA AÇÕES:
Quando precisar executar uma ação no dispositivo, responda ESTRITAMENTE em uma única linha no formato:
ACTION:<acao>|ARG:<argumento opcional>|CONFIRM:<true|false>

Exemplos:
- Para ligar a lanterna: ACTION:flashlight|ARG:on|CONFIRM:false
- Para checar bateria: ACTION:battery|ARG:|CONFIRM:false
- Para abrir um site: ACTION:url|ARG:https://google.com|CONFIRM:false
- Para ações sensíveis: ACTION:settings|ARG:reset|CONFIRM:true

Se a solicitação for uma conversa normal, apenas responda diretamente sem o prefixo ACTION.

Histórico de contexto e memórias guardadas:
$history

Usuário:
$user
""".trimIndent()
    }

    /**
     * Realiza requisições HTTP POST genéricas com tratamento de erros.
     */
    private fun post(url: String, headers: Map<String, String>, body: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 30000
            connection.readTimeout = 60000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (code !in 200..299) {
                error("HTTP $code: $text")
            }
            return JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Integração com a API da OpenAI (GPT-4o, GPT-3.5-turbo, etc.).
     */
    private fun callOpenAI(user: String, memory: List<String>): String {
        val key = secrets.get("api_key") ?: return "Configure sua chave da OpenAI nas configurações."
        val endpoint = secrets.get("endpoint")
            ?.takeIf { it.isNotBlank() }
            ?: "https://api.openai.com/v1/chat/completions"
        val model = secrets.get("model")
            ?.takeIf { it.isNotBlank() }
            ?: "gpt-4o-mini"

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", prompt(user, memory)))
            put(JSONObject().put("role", "user").put("content", user))
        }

        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.7)

        val response = post(
            endpoint,
            mapOf("Authorization" to "Bearer $key"),
            body.toString()
        )

        return response.optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "Não foi possível obter uma resposta válida da OpenAI."
    }

    /**
     * Integração com a API do Google Gemini.
     */
    private fun callGemini(user: String, memory: List<String>): String {
        val key = secrets.get("api_key") ?: return "Configure sua chave do Google Gemini nas configurações."
        val model = secrets.get("model")
            ?.takeIf { it.isNotBlank() }
            ?: "gemini-1.5-flash"

        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"

        val body = JSONObject().put(
            "contents",
            JSONArray().put(
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", prompt(user, memory)))
                )
            )
        )

        val response = post(endpoint, emptyMap(), body.toString())

        return response.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "Não foi possível obter resposta do Google Gemini."
    }

    /**
     * Integração com a API da Anthropic (Claude 3.5 Sonnet / Haiku).
     */
    private fun callAnthropic(user: String, memory: List<String>): String {
        val key = secrets.get("api_key") ?: return "Configure sua chave Anthropic nas configurações."
        val model = secrets.get("model")
            ?.takeIf { it.isNotBlank() }
            ?: "claude-3-5-haiku-20241022"

        val body = JSONObject()
            .put("model", model)
            .put("max_tokens", 1024)
            .put("system", prompt(user, memory))
            .put(
                "messages",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", user)
                )
            )

        val response = post(
            "https://api.anthropic.com/v1/messages",
            mapOf(
                "x-api-key" to key,
                "anthropic-version" to "2023-06-01"
            ),
            body.toString()
        )

        return response.optJSONArray("content")
            ?.optJSONObject(0)
            ?.optString("text")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "Não foi possível obter resposta da Anthropic."
    }

    /**
     * Utilitário para realizar o parse de chamadas de ferramentas enviadas pela IA.
     */
    fun parseToolCall(response: String): AiToolCall? {
        if (!response.startsWith("ACTION:")) return null
        return try {
            val parts = response.split("|")
            val action = parts.getOrNull(0)?.substringAfter("ACTION:")?.trim().orEmpty()
            val argument = parts.getOrNull(1)?.substringAfter("ARG:")?.trim().takeIf { it?.isNotBlank() == true }
            val confirmStr = parts.getOrNull(2)?.substringAfter("CONFIRM:")?.trim()
            val confirm = confirmStr?.lowercase() == "true"

            if (action.isNotEmpty()) {
                AiToolCall(action = action, argument = argument, confirmationRequired = confirm)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
