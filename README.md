# JARVIS Network Assistant — Android Alpha

Este é o início da adaptação Android do projeto Jarvis-V5-main. Não é o APK final e ainda não substitui todas as funções do programa original para computador.

## O que esta base implementa
- Interface Android escura com tema JARVIS.
- Exibição do estado da conexão e de dados de rede local disponíveis ao Android (IP, gateway e máscara quando reportados).
- Varredura limitada à sub-rede local conectada, verificando um conjunto pequeno de portas TCP comuns. Não tenta autenticar nem alterar dispositivos.
- Reconhecimento de fala em português usando o serviço de reconhecimento instalado no Android.
- Comandos de voz iniciais: “procurar dispositivos”, “informações da rede”.

## Limitações importantes
- Encontrar uma porta aberta não prova a identidade ou o estado real de um dispositivo. Firewalls, isolamento de clientes e restrições do roteador podem ocultar dispositivos.
- Controle de computadores, TVs, lâmpadas, tomadas e roteadores exige integrações específicas e autorização: por exemplo, Home Assistant, Matter, APIs oficiais de TV, ou um agente autorizado instalado no computador.
- Reconhecimento de fala pode usar um serviço do aparelho e exigir internet.
- Permissões e descoberta local variam entre versões de Android e fabricantes.
- O código original Python/desktop não foi migrado integralmente. A interface Tkinter, módulos específicos do Windows, PyAudio e dependências de visão precisam de adaptações individuais.

## Abrir no Android Studio
1. Descompacte o ZIP.
2. Abra a pasta `JarvisAndroid` no Android Studio.
3. Aguarde o Gradle Sync. É necessário Android SDK 35 e acesso à internet para baixar as ferramentas Gradle.
4. Execute em um celular Android conectado por USB ou crie um APK de depuração em **Build > Build APK(s)**.

## Próximas etapas
1. Testar descoberta no roteador/celular alvo.
2. Adicionar descoberta mDNS/DNS-SD e melhor identificação dos dispositivos.
3. Adicionar integração autorizada com Home Assistant/Matter ou APIs específicas.
4. Integrar módulos de IA do projeto original e configurar chaves/serviços necessários.
5. Gerar e testar APK assinado.
