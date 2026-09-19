# 🌍 gentrans

**An AI-powered translation tool on your command line.**

[//]: # "[![Release](https://img.shields.io/github/v/release/hiroaki404/gentrans?style=flat-square)](https://github.com/hiroaki404/gentrans/releases)"

[//]: # "[![License](https://img.shields.io/github/license/hiroaki404/gentrans?style=flat-square)](LICENSE)"

[//]: # "[![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20macOS%20%7C%20Windows-blue?style=flat-square)](#installation)"

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Usage](#-usage)
- [Configuration](#-configuration)
- [Command Reference](#-command-reference)
- [Limitations](#-limitations)
- [Work In Progress (WIP)](#-work-in-progress-wip)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🔍 About

`gentrans` is a Command-Line Interface (CLI) tool that leverages the power of Generative AI to provide high-quality text
translation directly in your terminal.

---

## ✨ Features

- **Direct Translation** - Translate text directly from command-line arguments.
- **Pipe Support** - Read text from standard input (stdin) to work seamlessly with pipes (`|`).
- **Text Summarization** - Summarize long texts before translation using the `-s` or `--summary` option.
- **Markdown Support** - Translate Markdown documents with `-m markdown` / `--format markdown`, keeping fenced code blocks and front matter intact.
- **Multiple AI Providers** - Support for multiple AI providers (e.g., OpenAI, Gemini).
- **Model Selection** - Select specific models for translation.
- **Environment Variable Support** - Configure API keys and settings via environment variables.

---

## 📋 Prerequisites

To run `gentrans`, you need to have **Java** installed on your system:

- **Java Development Kit (JDK) 17** or newer

---

## 📦 Installation

1. **Download and Extract**
   Download the latest release from the [GitHub Releases](https://github.com/hiroaki404/gentrans/releases) page and
   extract it.

2. **Add to PATH**
   Add the `bin` directory from the extracted folder to your system's PATH to make `gentrans` accessible from any
   terminal.

3. **Verify Installation**
   Run the following command to ensure it's installed correctly:
   ```bash
   gentrans --version
   ```

### 🛠️ Build from Source

For development or if you want to build from source:

```bash
git clone https://github.com/hiroaki404/gentrans.git
cd gentrans
./gradlew build
./gentrans-cli/build/install/gentrans/bin/gentrans --version
```

#### 🐛 Debug Mode

For debugging and development:

```bash
# Debug mode with verbose logging
./gradlew :gentrans-cli:run --args="hello"

# Explicit debug build
./gradlew -Pdebug=true installDist
./gentrans-cli/build/install/gentrans/bin/gentrans "hello"

# Production build (no debug logs)
./gradlew installDist
# or
./gradlew build
./gentrans-cli/build/install/gentrans/bin/gentrans "hello"
```

Note: If you are debugging, you will need ollama for testing.
If you want to use tracing for debug, you need to set up langfuse, and use `--trace` debug only option.
If you have a trouble, please `./gradlew clean` or make an issue.

To verify the ktor-free HTTP path (`ai.koog:http-client-java`) can actually reach the LLM
providers, run `./gradlew :gentrans-core:smokeTest`. It needs network access but no API key
(it expects auth failures), and is excluded from the normal `test` task and CI.

---

## Usage

### 💬 Basic Translation

Provide the text you want to translate as an argument:

```bash
$ gentrans "こんにちは世界"
# Expected: Hello, world

$ gentrans -t ja "Hello World"
# Expected: こんにちは世界

$ gentrans -t French "Hello"
# Expected: Bonjour
```

**Language Format Flexibility**: You can specify target languages in various formats since they are interpreted by the
LLM. For example: `English`, `en`, `Japanese`, `ja`, `日本語`, `French`, `fr`, etc.

### 🔀 Piping from Standard Input

Use `gentrans` as part of a command pipeline:

```bash
$ echo "CLIツールは開発者にとって強力な武器です。" | gentrans
# Expected: CLI tools are powerful weapons for developers.
```

Translate the content of a file:

```bash
$ cat document_ja.txt | gentrans > document_en.txt
```

or clipboard

```bash
$ pbpaste | gentrans # macOS
```

### 📝 Translating Markdown

Use `-m markdown` (or `--format markdown`) to translate Markdown documents. Fenced code blocks and YAML front matter
are kept verbatim, and the LLM is instructed to preserve Markdown syntax:

```bash
$ cat README.md | gentrans --format markdown --to Japanese > README.ja.md
```

### 🤖 Specifying AI Provider and Model

You can specify the AI provider and model to use for translation.

```bash
# Use Gemini
$ gentrans --provider google "こんにちは"

# Use a specific OpenAI model
$ gentrans --provider openai --model gpt-4o "こんにちは"
```

---

## Configuration

Configuration can be done via command-line flags and environment variables.

Configuration settings are prioritized in the following order:

1. **Command-line flags** (highest priority)
2. **Environment variables**
3. **Default values** (lowest priority)

This means that any setting provided as a command-line flag will override the corresponding environment variable, which
in turn overrides the default value.

If no configuration is specified, the following default values will be used:

- **Default Provider**: `openai`
- **Default Model**: `gpt-4o`
- **Default Native Language**: `English`
- **Default Second Language**: `English`

**Note:** Currently, `gentrans` does not support registering and switching between multiple providers or models. You can
only use one provider and model configuration at a time.

### Command-line flags:

- `--apikey`: Your secret API Key for the translation service.
- `--provider`: AI Provider to use. Supported providers are `google`, `openai`, `anthropic`, `meta`, `alibaba`,
  `openrouter`, and `ollama`.
- `--model`: AI model to use. This tool relies on the [Koog](https://github.com/JetBrains/koog) library, so only models
  supported by Koog can be used. For a detailed list, please refer to the official Koog documentation (
  e.g., [GoogleModels.kt](https://github.com/JetBrains/koog/blob/develop/prompt/prompt-executor/prompt-executor-clients/prompt-executor-google-client/src/commonMain/kotlin/ai/koog/prompt/executor/clients/google/GoogleModels.kt)).
  Representative models include `gemini-2.0-flash`, `gpt-4o`, `o3`, `gpt-4o-mini`, `claude-3-opus`, `claude-sonnet-4-0`,
  `gemma3n:latest`, and `llama3.2:latest`.
- `-m, --format`: Input text format. `plain` (default) or `markdown`. With `markdown`, fenced code blocks and front
  matter are kept verbatim and Markdown syntax is preserved.

### Environment variables:

- `GENTRANS_API_KEY`: Your secret API Key for the translation service.
- `GENTRANS_PROVIDER`: AI Provider to use (e.g., `openai`, `google`).
- `GENTRANS_MODEL`: AI model to use (e.g., `gpt-4o`, `gemini-2.0-flash`).
- `GENTRANS_NATIVE_LANGUAGE`: Your native language (e.g., `Japanese`, `English`). You can use various formats like
  `Japanese`, `ja`, `日本語`, etc.
- `GENTRANS_SECOND_LANGUAGE`: Your second language (e.g., `English`, `Japanese`). You can use various formats like
  `English`, `en`, `英語`, etc.

Regarding GENTRANS_NATIVE_LANGUAGE and GENTRANS_SECOND_LANGUAGE, there is an explanation in the Automatic Bidirectional
Translation section below.

### Usage Examples:

```bash
# Using environment variables
export GENTRANS_API_KEY="your-api-key-here"
export GENTRANS_PROVIDER="openai"
export GENTRANS_MODEL="gpt-4o"
gentrans "こんにちは世界"

# Using command-line flags (overrides environment variables)
gentrans --apikey "your-api-key" --provider "gemini" --model "gemini-2.0-flash" "こんにちは世界"

# Manual target language specification (overrides automatic mode)
gentrans -t "French" "Hello World"  # → Bonjour
```

### 🔄 Automatic Language Detection Translation

Configure your native and second languages for automatic translation. The translation behavior follows these rules:

- **When `-t` option is specified**: Translates to the specified target language (manual mode)
- **When `-t` option is not specified**: Automatic translation mode based on input language detection:
    - If input is in your **native language** → translates to your **second language**
    - If input is in your **second language** → translates to your **native language**
    - If input is in **any other language** → translates to your **native language**
- **Default behavior**: Both native and second languages default to English. Without the `-t` option, English input
  returns as-is, while all other languages are translated to English.

```bash
# Configure languages
export GENTRANS_NATIVE_LANGUAGE="Japanese"
# export GENTRANS_SECOND_LANGUAGE="English" # Default is English, so no need to set this

# Automatic translation based on input language
gentrans "Hello World"        # → こんにちは世界 (English → Japanese)
gentrans "こんにちは世界"      # → Hello World (Japanese → English)
gentrans "Bonjour"            # → こんにちは (French → Japanese)
gentrans "Hola"               # → こんにちは (Spanish → Japanese)
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

---

## 📖 Command Reference

### 🌍 Main Command: Translation

```
USAGE:
    gentrans [OPTIONS] [TEXT_TO_TRANSLATE]

ARGS:
    <TEXT_TO_TRANSLATE>    Text to translate. Reads from stdin if not provided.

OPTIONS:
        --apikey <APIKEY>  API key for the AI provider.
        --provider <PROVIDER>
                           AI provider to use. Supported providers are `google`, `openai`, `anthropic`, `meta`, `alibaba`, `openrouter`, and `ollama`.
        --model <MODEL>    AI model to use. e.g. `gemini-2.0-flash`, `gpt-4o`, `claude-3-opus`, `gemma3n:latest`, `llama3.2:latest`. Supported models depend on the Koog library. See documentation for details.
    -t, --to <LANGUAGE>    Specify the target language. Since the language is interpreted by an LLM, you can use various formats like `English`, `en`, or even `日本語`.
    -s, --summary          Enable text summarization before translation. Useful for long texts.
    -m, --format <FORMAT>  Input text format. `plain` (default) or `markdown`. With `markdown`, fenced code blocks and front matter are kept verbatim and Markdown syntax is preserved.
    -h, --help             Print help information
        --version          Print version information
```

---

## ⚠️ Limitations

Please be aware of the following limitations when translating long texts:

### Long Text Input Constraints

- **Language Detection Accuracy**: For long texts, automatic language detection is applied based on the initial chunk,
  which may not accurately represent the language of the entire text
- **Contextual Continuity**: Since translation is performed in chunks, long texts may occasionally experience
  inconsistent connections between translated segments
- **Line-Based Splitting**: Currently, long text chunking is performed by line breaks (applies to `plain` format), so
  translation of long passages without line breaks may not be handled properly

Due to these limitations, when translating long texts, it is recommended to either structure the text with appropriate
line breaks or divide the text into manageable segments before translation.

### Markdown Input

- Only fenced code blocks and YAML front matter are protected from translation; inline code spans, URLs, and image
  paths are not shielded and instead rely on the translation prompt's instructions to be left untranslated.
- A single Markdown block (e.g. a very long table or list) that alone exceeds the chunk size limit is translated as
  one chunk without being split further, since splitting it could break its structure.
- Combining `--format markdown` with `-s`/`--summary` is not supported: summarization may alter or drop the
  placeholder tokens used to protect blocks, causing translation to fail.

---

## 🚧 Work In Progress (WIP)

The following features are planned but not yet implemented:

- **Flexible Configuration System:**
    - Configuration via a file (`~/.config/gentrans/config.toml`).
    - Multiple provider/model profiles with easy switching.
- **Advanced Translation Options:**
    - `-f`, `--from <LANGUAGE>`: Specify the source language.
    - `-T`, `--tone <TONE>`: Define the translation tone/style (e.g., `formal`, `casual`, `technical`).
- **`config` Subcommand:**
    - A dedicated command (`gentrans config`) to easily manage settings (`set`, `get`, `list`).

---

## 🤝 Contributing

Contributions are welcome! Please feel free to:

- 🐛 **Report bugs** by opening an issue
- 💡 **Suggest features** through discussions
- 🔧 **Submit pull requests** to improve the tool

Before contributing, please check our [contribution guidelines](CONTRIBUTING.md).

---

## 📜 Disclaimer

- **API Keys**: You are responsible for managing your own API keys. This tool does not store or transmit your keys to
  any third party other than the selected AI provider. Please be aware of the security risks when passing API keys as
  command-line arguments or setting them as environment variables.
- **Translation Quality**: The quality of translations depends on the AI provider and model. We are not responsible for
  any inaccuracies or errors in the translated text.
- **Usage Costs**: Use of AI provider APIs may incur costs. You are responsible for all costs associated with your use
  of the APIs.
- **No Warranty**: This tool is provided "as is" without any warranties. The developer is not responsible for any damage
  or loss resulting from the use of this tool.

---

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).

