# 🌍 gentrans

**An AI-powered translation tool on your command line.**

[//]: # "[![Release](https://img.shields.io/github/v/release/hiroaki404/gentrans?style=flat-square)](https://github.com/hiroaki404/gentrans/releases)"

[//]: # "[![License](https://img.shields.io/github/license/hiroaki404/gentrans?style=flat-square)](LICENSE)"

[//]: # "[![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20macOS%20%7C%20Windows-blue?style=flat-square)](#installation)"

---

> ⚠️ **Work In Progress** ⚠️
>
> This tool is under active development. Commands, flags, and APIs are subject to change without notice. Please use it
> with caution.

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Usage](#-usage)
- [Configuration](#-configuration)
- [Command Reference](#-command-reference)
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
./build/install/gentrans/bin/gentrans --version
```

---

## Usage

### 💬 Basic Translation

Provide the text you want to translate as an argument:

```bash
$ gentrans "こんにちは世界"
or
$ gentrans こんにちは世界
# Expected: Hello, world
$ gentrans -t "ja" "Hello World"
```

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

This means that any setting provided as a command-line flag will override the corresponding environment variable.

If the provider or model is not specified, the following default values will be used:

- **Default Target Language**: `English`
- **Default Provider**: `openai`
- **Default Model**: `gpt-4o`

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
  and `llama3.2:3b`.

### Environment variables:

- `GENTRANS_API_KEY`: Your secret API Key for the translation service.
- `GENTRANS_PROVIDER`: AI Provider to use (e.g., `openai`, `google`).
- `GENTRANS_MODEL`: AI model to use (e.g., `gpt-4o`, `gemini-2.0-flash`).

### Usage Examples:

```bash
# Using environment variables
export GENTRANS_API_KEY="your-api-key-here"
export GENTRANS_PROVIDER="openai"
export GENTRANS_MODEL="gpt-4o"
gentrans "こんにちは世界"

# Using command-line flags (overrides environment variables)
gentrans --apikey "your-api-key" --provider "gemini" --model "gemini-2.0-flash" "こんにちは世界"
```

### ✅ Verified Combinations

The following combinations of providers and models have been tested and are known to work:

| Provider | Model              |
|----------|--------------------|
| `openai` | `gpt-4o`           |
| `google` | `gemini-2.0-flash` |
| `ollama` | `llama3.2`         |

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
        --model <MODEL>    AI model to use. Supported models depend on the Koog library. See documentation for details.
    -t, --to <LANGUAGE>    Specify the target language. Since the language is interpreted by an LLM, you can use various formats like `English`, `en`, or even `日本語`.
    -h, --help             Print help information
        --version          Print version information
```

---

## 🚧 Work In Progress (WIP)

The following features are planned but not yet implemented:

- **Flexible Configuration System:**
    - Configuration via a file (`~/.config/gentrans/config.toml`).
    - Multiple provider/model profiles with easy switching.
- **Advanced Translation Options:**
    - `-f`, `--from <LANGUAGE>`: Specify the source language.
    - `-s`, `--style <STYLE>`: Define the translation style (e.g., `formal`, `casual`).
- **`config` Subcommand:**
    - A dedicated command (`gentrans config`) to easily manage settings (`set`, `get`, `list`, `path`).

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

