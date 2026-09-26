# 🌍 gentrans

**An AI-powered translation tool for the command line and IntelliJ IDEA / Android Studio.**

---

## 📋 Table of Contents

- [About](#-about)
- [IntelliJ IDEA and Android Studio Plugin](#intellij-idea-and-android-studio-plugin)
  - [Plugin Features](#-plugin-features)
  - [Supported IDEs](#-supported-ides)
  - [Plugin Installation](#-plugin-installation)
  - [Plugin Settings](#-plugin-settings)
  - [Plugin Usage](#-plugin-usage)
  - [Plugin Limitations](#-plugin-limitations)
- [Command-Line Interface (CLI)](#command-line-interface-cli)
  - [CLI Features](#-cli-features)
  - [CLI Prerequisites](#-cli-prerequisites)
  - [CLI Installation](#-cli-installation)
  - [CLI Usage](#cli-usage)
  - [CLI Configuration](#cli-configuration)
  - [Command Reference (abridged)](#-command-reference-abridged)
  - [CLI Limitations](#cli-limitations)
  - [Work In Progress (WIP)](#-work-in-progress-wip)
- [Build from Source](#-build-from-source)
- [Data Privacy](#-data-privacy)
- [Contributing](#-contributing)
- [Disclaimer](#-disclaimer)
- [License](#-license)

---

## 🔍 About

`gentrans` has two forms: a command-line interface (CLI) for translating terminal input, and an IntelliJ IDEA / Android Studio plugin for previewing Markdown translations in the IDE. They share version `0.4.0`; their ZIP files are published together in one GitHub Release.

---

# IntelliJ IDEA and Android Studio Plugin

## ✨ Plugin Features

- **Markdown Translation Preview** - **Translate Markdown** opens the **GenTrans** tool window and renders translated chunks progressively.
- **Selection or Document Translation** - In an editor, a non-empty selection is translated; otherwise the whole editor document is translated. From the Project View, the whole file is translated, including unsaved changes if it is open in an editor.
- **Preview Toolbar** - **Copy**, **Save as**, **Cancel**, and **Re-run**.
- **Long-text Confirmation** - A confirmation dialog is shown at five or more chunks.
- **Protected Markdown** - In Markdown mode, YAML front matter at the very start of the translated text and fenced code blocks are replaced with placeholders and restored locally. Indented code blocks, HTML, inline code, URLs, and image paths are sent as-is.

## 💻 Supported IDEs

- **IntelliJ IDEA 2026.1+ (build 261+)** or **Android Studio 2026.1+ (build 261+)**
- No separate JDK installation is needed to use the plugin; the supported IDE provides its bundled JetBrains Runtime (JBR).

## 📦 Plugin Installation

1. Download `gentrans-idea-plugin-<version>.zip` from [GitHub Releases](https://github.com/hiroaki404/gentrans/releases).
2. Open **Settings > Plugins**, select the gear icon, and choose **Install Plugin from Disk...**.
3. Select the ZIP file and restart the IDE if prompted.

The plugin is distributed through GitHub Releases only and is not published on the JetBrains Marketplace. Updates are not automatic; manually install a newer ZIP over the existing plugin. The IDE may show an unsigned-plugin warning.

## 🔧 Plugin Settings

Open **Settings > Tools > GenTrans** to configure Provider, Model, API key, Target language, Native language, Second language, and Ollama base URL.

The defaults are provider **OpenAI**, model `gpt-4o-mini`, native language **Japanese**, and second language **English**. The API key field is disabled for Ollama, and the Ollama base URL is shown only when Ollama is selected.

The first release offers **OpenAI**, **Google**, **Anthropic**, and **Ollama**. Google uses Koog's beta client (`1.2.0-beta`), so its behavior may change between releases.

API keys are stored through the IDE PasswordSafe under a provider-specific GenTrans service name. The persistent plugin settings state stores the other fields in `gentrans.xml`; it has no API-key field.

## 🚀 Plugin Usage

Your text is sent to the configured AI provider — see [Data Privacy](#-data-privacy).

1. Set the provider, model, API key, and language settings in **Settings > Tools > GenTrans**.
2. Right-click a Markdown file in an editor or in the Project View, and select **Translate Markdown**.
3. Review the result in the **GenTrans** tool window as chunks arrive.
4. Use **Copy** or **Save as**. Save as creates `<nameWithoutExtension>.<target-language>.md` beside the source. It uses the model's normalized English target-language name, lowercased with whitespace replaced by hyphens; for example, `README.japanese.md`, even when the setting was `ja`. It asks before overwriting an existing file. If target-language decision has not completed, Save as does nothing. For a selection translation, it saves only the translated selection.

## ❗ Plugin Limitations

- The preview renders Markdown as HTML but does not add syntax highlighting for code fences.
- Image URLs in the preview may be fetched by the IDE.
- The plugin has no summary-specific or comment-translation workflow. Selection translation is supported.
- The plugin has no proxy settings and its UI is English only.
- The target language comes from settings. Leaving it empty uses the configured native and second languages to decide the target language.
- Requires the IDE's bundled Markdown plugin to be enabled.

---

# Command-Line Interface (CLI)

## ✨ CLI Features

- **Direct Translation** - Translate text directly from command-line arguments.
- **Pipe Support** - Read text from standard input (stdin) to work with pipes (`|`).
- **Text Summarization** - Summarize long texts before translation with `-s` or `--summary`.
- **Markdown Support** - Translate Markdown with `-m markdown` / `--format markdown`, protecting YAML front matter and fenced code blocks.
- **AI Providers** - Use OpenAI, Google, Anthropic, or Ollama.
- **Model Selection** - Choose a provider-supported model.
- **Environment Variable Support** - Configure the CLI through environment variables.

## 📋 CLI Prerequisites

- **Java Development Kit (JDK) 17** or newer

## 📦 CLI Installation

1. Download `gentrans-<version>.zip` from [GitHub Releases](https://github.com/hiroaki404/gentrans/releases) and extract it. This is the CLI ZIP, not `gentrans-idea-plugin-<version>.zip`.
2. Add its `bin` directory to your system PATH.
3. Verify the installation:

```bash
gentrans --version
```

---

## CLI Usage

Your text is sent to the configured AI provider — see [Data Privacy](#-data-privacy).

### 💬 Basic Translation

```bash
$ gentrans "こんにちは世界"
# Expected: Hello, world
$ gentrans -t ja "Hello World"
# Expected: こんにちは世界
$ gentrans -t French "Hello"
# Expected: Bonjour
```

Target languages are interpreted by the LLM, so formats such as `English`, `en`, `Japanese`, `ja`, `日本語`, `French`, and `fr` can be used.

### 🔀 Piping from Standard Input

```bash
$ echo "CLIツールは開発者にとって強力な武器です。" | gentrans
$ cat document_ja.txt | gentrans > document_en.txt
$ pbpaste | gentrans # macOS
```

### 📝 Translating Markdown

```bash
$ cat README.md | gentrans --format markdown --to Japanese > README.ja.md
```

### 🤖 Specifying AI Provider and Model

```bash
$ gentrans --provider google "こんにちは"
$ gentrans --provider openai --model gpt-4o "こんにちは"
```

---

## CLI Configuration

Configuration is resolved in this order: command-line flags, environment variables, then built-in defaults. There is no local configuration source yet. The defaults are `openai`, `gpt-4o`, and English for both native and second languages. With the defaults (both English), all input is translated into English.

**Note:** The CLI does not support registering and switching between multiple providers or models. You can only use one provider and model configuration at a time.

### Command-line flags

- `--apikey`: API key for the selected provider.
- `--provider`: The CLI accepts `google`, `openai`, `anthropic`, `meta`, `alibaba`, `openrouter`, and `ollama`. Clients are implemented only for `google`, `openai`, `anthropic`, and `ollama`; choosing `meta`, `alibaba`, or `openrouter` fails during client construction with `Unknown provider: <name>`.
- `--model`: AI model to use. This tool relies on the [Koog](https://github.com/JetBrains/koog) library, so only models
  supported by Koog can be used. For a detailed list, please refer to the official Koog documentation (
  e.g., [GoogleModels.kt](https://github.com/JetBrains/koog/blob/develop/prompt/prompt-executor/prompt-executor-clients/prompt-executor-google-client/src/commonMain/kotlin/ai/koog/prompt/executor/clients/google/GoogleModels.kt)).
  Representative models include `gemini-2.0-flash`, `gpt-4o`, `o3`, `gpt-4o-mini`, `claude-3-opus`, `claude-sonnet-4-0`,
  `gemma3n:latest`, and `llama3.2:latest`.
- `-m, --format`: `plain` (default) or `markdown`.

### Environment variables

- `GENTRANS_API_KEY`: API key for the selected provider.
- `GENTRANS_PROVIDER`: Provider name, such as `openai`, `google`, `anthropic`, or `ollama`.
- `GENTRANS_MODEL`: Model name for the selected provider.
- `GENTRANS_NATIVE_LANGUAGE`: Native language used when deciding a target language.
- `GENTRANS_SECOND_LANGUAGE`: Second language used when deciding a target language.

Native and second languages can only be set via environment variables in the CLI. Language values can use forms such as `Japanese`, `ja`, or `日本語`.

```bash
export GENTRANS_API_KEY="your-api-key-here"
export GENTRANS_PROVIDER="openai"
export GENTRANS_MODEL="gpt-4o"
gentrans "こんにちは世界"

# Command-line flags override environment variables.
gentrans --apikey "your-api-key" --provider "google" --model "gemini-2.0-flash" "こんにちは世界"
```

### 🔄 Automatic Language Detection Translation

With `-t`, gentrans uses the requested target. Without it, detected native-language input translates to the second language, second-language input translates to the native language, and other input translates to the native language. With the defaults (both English), all input is translated into English.

```bash
export GENTRANS_NATIVE_LANGUAGE="Japanese"
# GENTRANS_SECOND_LANGUAGE defaults to English.
gentrans "Hello World" # → こんにちは世界
gentrans "こんにちは世界" # → Hello World
gentrans "Bonjour" # → こんにちは
```

### ✅ Verified Combinations

The following combinations of providers and models have been tested and are known to work:

| Provider | Model                   |
|----------|-------------------------|
| `openai` | `gpt-4o`                |
| `openai` | `gpt-4o-mini`           |
| `google` | `gemini-2.0-flash`      |
| `google` | `gemini-2.0-flash-lite` |
| `ollama` | `llama3.2:latest`       |
| `ollama` | `gemma3n:latest`        |

Google uses Koog's beta client (`1.2.0-beta`), so its behavior may change between releases.

## 📖 Command Reference (abridged)

```
USAGE:
    gentrans [OPTIONS] [TEXT_TO_TRANSLATE]

ARGS:
    <TEXT_TO_TRANSLATE>    Text to translate. Reads from stdin if not provided.

OPTIONS:
        --apikey <APIKEY>  API key for the AI provider.
        --provider <PROVIDER>
                           Accepted keys: google, openai, anthropic, meta, alibaba, openrouter, ollama.
                           Client implementations: google, openai, anthropic, ollama.
        --model <MODEL>    Model availability depends on the selected provider and Koog client.
    -t, --to <LANGUAGE>    Specify the target language.
    -s, --summary          Enable text summarization before translation.
    -m, --format <FORMAT>  Input format: plain (default) or markdown.
    -h, --help             Print help information.
        --version          Print version information.
```

## CLI Limitations

### Long Text Input Constraints

- Language detection for long input uses the first non-placeholder chunk, which might not represent the whole text.
- Translation is performed in chunks, so continuity between translated segments can be inconsistent.
- Plain-text input is split at line breaks; a single long line is not split further and can exceed the chunk size.

### Markdown Input

- Only fenced code blocks and YAML front matter are protected. Inline code spans, URLs, and image paths are not shielded.
- A single Markdown block exceeding the chunk size limit is not split further.
- Combining `--format markdown` with `-s`/`--summary` is not recommended: the summary may drop protected blocks, and if it duplicates or alters a placeholder the command fails with "Markdown placeholder mismatch detected".

## 🚧 Work In Progress (WIP)

Planned CLI work includes file-based configuration, provider/model profiles, source-language and tone options, and a `gentrans config` subcommand.

---

# Shared Information

## 🔨 Build from Source

```bash
# CLI
./gradlew :gentrans-cli:installDist
./gentrans-cli/build/install/gentrans/bin/gentrans --version
# Plugin (needs a local Android Studio install, see below)
./gradlew :gentrans-idea-plugin:buildPlugin
```

The plugin ZIP is written to `gentrans-idea-plugin/build/distributions/`. The plugin module uses a JDK 21 toolchain. Building the plugin needs a local Android Studio installation; `buildPlugin` uses Android Studio at `~/Applications/Android Studio.app` by default. IntelliJ IDEA at `~/Applications/IntelliJ IDEA.app` by default is used by `verifyPlugin` and `runIdea`.

### 🐛 Debug Mode

```bash
# Debug build; --trace is effective when passed through --args.
./gradlew :gentrans-cli:run --args="hello"
# Debug build; --trace is effective in the installed binary.
./gradlew -Pdebug=true installDist
./gentrans-cli/build/install/gentrans/bin/gentrans "hello"
# Release build; --trace is ignored.
./gradlew installDist
```

`IS_DEBUG` affects only `--trace`. `--trace` is effective only with `:gentrans-cli:run` or a debug-built binary, and requires Langfuse setup.

`./gradlew test` also runs local Ollama `gemma3n:latest` integration tests; set `CI=true` to skip them. The real-HTTP checks in `./gradlew :gentrans-core:smokeTest` target only the OpenAI and Google endpoints. This manual check needs network access but no API key (it expects authentication failures), and it is excluded from regular `test` and CI.

## 🔒 Data Privacy

- Source-language detection sends the first non-placeholder chunk to the selected provider. For every translation, target-language decision is a second LLM request to that provider. The detected and target languages are normalized to standard English names, including when `-t` or Settings use another form. The target-language request includes the detected language name, the native and second languages or explicit target setting, and the preceding detection exchange, including that chunk.
- The plugin sends the selected text or, with no selection, the entire editor document, including unsaved changes. The CLI sends text supplied as an argument or through standard input. Long input can be sent in multiple translation requests.
- Only Markdown mode (the plugin and CLI `-m markdown`) replaces YAML front matter at the beginning of the text to translate and fenced code blocks with placeholders before sending them, then restores them locally. For a selection, front matter is protected only when the selection starts with `---`. All other content, including indented code blocks, HTML, inline code, URLs, and image paths, is sent as-is. The CLI's default plain mode performs no replacement. Image URLs in the plugin preview may be fetched by the IDE.
- The Ollama CLI uses the fixed URL `http://localhost:11434`. The plugin uses that URL when its Ollama base URL is empty. Depending on the Ollama setup, for example a cloud-hosted model, input can leave the machine; setting the plugin URL to another host sends input there.
- gentrans does not collect analytics or usage telemetry. In CLI debug builds only (`:gentrans-cli:run` or `-Pdebug=true`), `--trace` enables OpenTelemetry with the Langfuse exporter in verbose mode and sends full prompts and responses, including input text, to the configured Langfuse host. It requires `LANGFUSE_PUBLIC_KEY` and `LANGFUSE_SECRET_KEY`; the host is selected from `LANGFUSE_HOST`, then `LANGFUSE_BASE_URL`, then `https://cloud.langfuse.com`. Release builds ignore `--trace`, and the plugin has no tracing.
- API keys are supplied only to the selected provider's client. The plugin stores them in IDE PasswordSafe; CLI keys may be passed as a command-line option or environment variable.
- For confidential documents, review the selected provider's terms and data-handling policy before use. Providers differ in how they handle API inputs, including whether inputs are used for training.

## 🤝 Contributing

Contributions are welcome: report bugs by opening an issue, suggest features through discussions, or submit pull requests.

## 📜 Disclaimer

- **API Keys**: You are responsible for managing your API keys and for the security risks of CLI arguments and environment variables.
- **Translation Quality**: Translation quality depends on the selected provider and model.
- **Usage Costs**: Provider APIs can incur costs, for which you are responsible.
- **No Warranty**: This tool is provided "as is" without warranties. The developer is not responsible for damage or loss resulting from its use.

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE). Licenses of bundled third-party software are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md), which is also included in the CLI and plugin ZIP files. Release notes are in [CHANGELOG.md](CHANGELOG.md).
