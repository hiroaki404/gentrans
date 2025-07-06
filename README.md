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

### 🔽 Download Release

1. **Download the Release**

   Download `gentrans-1.0-SNAPSHOT.zip` from the [GitHub Releases](https://github.com/hiroaki404/gentrans/releases)
   page.

2. **Extract the Archive**

   Extract the contents to your desired location.

3. **Test the Installation**

   Navigate to the extracted directory and run:

   ```bash
   ./gentrans --version
   ```

### 🔧 Add to PATH

To run `gentrans` from any directory, add it to your system's PATH:

#### 🐧 **Linux/macOS**

```bash
# Add to your shell configuration file (~/.bashrc, ~/.zshrc, etc.)
export PATH="/path/to/gentrans/bin:$PATH"

# Apply changes
source ~/.bashrc  # or ~/.zshrc
```

#### 🪟 **Windows**

1. Search for "Environment Variables" in the Start menu
2. Select "Edit the system environment variables"
3. Click "Environment Variables..."
4. Find "Path" variable and click "Edit..."
5. Click "New" and add the path to `gentrans\bin` directory
6. Click "OK" to save

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
$ gentrans --provider gemini "こんにちは"

# Use a specific OpenAI model
$ gentrans --provider openai --model gpt-4 "こんにちは"
```

---

## Configuration

Configuration can be done via command-line flags and environment variables.

**Note:** Currently, `gentrans` does not support registering and switching between multiple providers or models. You can only use one provider and model configuration at a time.

### Command-line flags:

- `--apikey`: Your secret API Key for the translation service.
- `--provider`: AI Provider to use (e.g., `openai`, `gemini`).
- `--model`: AI model to use (e.g., `gpt-4`, `gemini-pro`).

### Environment variables:

- `GENTRANS_API_KEY`: Your secret API Key for the translation service.
- `GENTRANS_PROVIDER`: AI Provider to use (e.g., `openai`, `gemini`).
- `GENTRANS_MODEL`: AI model to use (e.g., `gpt-4`, `gemini-pro`).

### Usage Examples:

```bash
# Using environment variables
export GENTRANS_API_KEY="your-api-key-here"
export GENTRANS_PROVIDER="openai"
export GENTRANS_MODEL="gpt-4"
gentrans "こんにちは世界"

# Using command-line flags (overrides environment variables)
gentrans --apikey "your-api-key" --provider "gemini" --model "gemini-pro" "こんにちは世界"
```

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
                           AI provider to use (e.g., openai, gemini).
        --model <MODEL>    AI model to use.
    -h, --help             Print help information
        --version          Print version information
```

---

## 🚧 Work In Progress (WIP)

The following features are planned but not yet implemented:

- **Flexible Configuration System:**
  - Configuration via a file (`~/.config/gentrans/config.toml`).
  - A clear precedence order for settings (flags > env vars > config file).
  - Multiple provider/model profiles with easy switching.
- **Advanced Translation Options:**
  - `-t`, `--to`: Specify the target language.
  - `-f`, `--from`: Specify the source language.
  - `-s`, `--style`: Define the translation style (e.g., `formal`, `casual`).
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

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).
