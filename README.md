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
- [Usage](#-usage-wip)
- [Configuration](#-configuration-wip)
- [Command Reference](#-command-reference)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🔍 About

`gentrans` is a Command-Line Interface (CLI) tool that leverages the power of Generative AI to provide high-quality text
translation directly in your terminal. It's designed to be simple for basic use, yet powerful and flexible for scripting
and advanced workflows.

---

## ✨ Features

- 🚧 **Direct Translation** - _\[WIP\]_ Translate text directly from command-line arguments
- 🚧 **Pipe Support** - _\[WIP\]_ Read text from standard input (stdin) to work seamlessly with pipes (`|`)
- 🚧 **Multiple AI Providers** - _\[WIP\]_ Support for multiple AI providers (e.g., OpenAI, Gemini)
- 🚧 **Model Selection** - _\[WIP\]_ Select specific models for translation
- 🚧 **Flexible Configuration** - _\[WIP\]_ Configure via config file, environment variables, and command-line flags

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

## 🚧 Usage [WIP]

> ⚠️ **All usage features are currently under development** ⚠️
>
> The examples below represent the planned functionality but are not yet implemented.

### 💬 Basic Translation _(Planned)_

Provide the text you want to translate as an argument:

```bash
$ gentrans "こんにちは、世界"
# Expected: Hello, world
# Status: 🚧 Not implemented yet
```

### 🔀 Piping from Standard Input _(Planned)_

Use `gentrans` as part of a command pipeline:

```bash
$ echo "CLIツールは開発者にとって強力な武器です。" | gentrans
# Expected: CLI tools are powerful weapons for developers.
# Status: 🚧 Not implemented yet
```

Translate the content of a file:

```bash
$ cat document_ja.txt | gentrans --to en-US > document_en.txt
# Status: 🚧 Not implemented yet
```

### 🎯 Specifying Languages and Style _(Planned)_

Control the translation with options:

- `-t`, `--to`: Set the target language _(🚧 Planned)_
- `-f`, `--from`: Set the source language (auto-detected by default) _(🚧 Planned)_
- `-s`, `--style`: Define the translation style (e.g., `formal`, `casual`) _(🚧 Planned)_

```bash
# Translate to German in a formal style
$ gentrans --to de --style formal "This document is an official report."
# Expected: Dieses Dokument ist ein offizieller Bericht.
# Status: 🚧 Not implemented yet
```

---

## 🚧 Configuration [WIP]

> ⚠️ **All configuration features are currently under development** ⚠️
>
> The configuration system below represents the planned functionality but is not yet implemented.

`gentrans` will be configurable in three ways, with the following **planned order of precedence**:

1. **🏅 Command-line Flags** (e.g., `--to fr`) - _Highest priority_ _(🚧 Planned)_
2. **🌍 Environment Variables** (e.g., `GENTRANS_API_KEY`) - _Medium priority_ _(🚧 Planned)_
3. **📁 Configuration File** - _Lowest priority_ _(🚧 Planned)_

### 📁 Configuration File _(Planned)_

Set your API key and default preferences in a configuration file.

**Planned File Locations:**

- **Linux/macOS:** `~/.config/gentrans/config.toml` _(🚧 Not implemented yet)_
- **Windows:** `%APPDATA%\gentrans\config\config.toml` _(🚧 Not implemented yet)_

**Example `config.toml` _(Planned format)_:**

```toml
# Your secret API Key for the translation service (Required)
API_KEY = "sk-xxxxxxxxxxxxxxxxxxxx"

# AI Provider settings
# Supported providers: "openai", "gemini"
provider = "openai"

# Model selection
# OpenAI models: "gpt-4", "gpt-3.5-turbo", etc.
# Gemini models: "gemini-pro", "gemini-1.5-pro", etc.
model = "gpt-4"

# Default language to translate to
default_target_lang = "en-US"

# Default translation style (e.g., "formal", "casual", "technical")
default_style = "casual"
```

### 🌍 Environment Variables _(Planned)_

Override settings from the config file using environment variables:

| Variable               | Description                  | Status             |
|------------------------|------------------------------|--------------------|
| `GENTRANS_API_KEY`     | Your API key                 | 🚧 Not implemented |
| `GENTRANS_PROVIDER`    | AI provider (openai, gemini) | 🚧 Not implemented |
| `GENTRANS_MODEL`       | AI model to use              | 🚧 Not implemented |
| `GENTRANS_TARGET_LANG` | Default target language      | 🚧 Not implemented |
| `GENTRANS_STYLE`       | Default translation style    | 🚧 Not implemented |

### 🛠️ Config Subcommand _(Planned)_

Manage your configuration file easily from the command line:

```bash
# Set your API key
$ gentrans config set API_KEY "sk-xxxxxxxxxxxxxxxxxxxx"
# Status: 🚧 Not implemented yet

# Set AI provider
$ gentrans config set provider "gemini"
# Status: 🚧 Not implemented yet

# Set specific model
$ gentrans config set model "gpt-4"
# Status: 🚧 Not implemented yet

# Get a specific value
$ gentrans config get API_KEY
# Expected: sk-xxxxxxxxxxxxxxxxxxxx
# Status: 🚧 Not implemented yet

# List all current settings
$ gentrans config list
# Expected:
# API_KEY = "sk-xxxxxxxxxxxxxxxxxxxx"
# provider = "openai"
# model = "gpt-4"
# default_target_lang = "en-US"
# Status: 🚧 Not implemented yet

# Find where the config file is stored
$ gentrans config path
# Expected: /home/user/.config/gentrans/config.toml
# Status: 🚧 Not implemented yet
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
    -t, --to <LANG>        Target language (e.g., en-US, de, zh) [default: en-US]
    -f, --from <LANG>      Source language (auto-detected if not set)
    -s, --style <STYLE>    Translation style (e.g., formal, casual) [default: casual]
    -p, --provider <PROVIDER>  AI provider (openai, gemini) [WIP]
    -m, --model <MODEL>    AI model to use [WIP]
    -h, --help             Print help information
        --version          Print version information
```

### ⚙️ Config Subcommand

```
USAGE:
    gentrans config <SUBCOMMAND>

SUBCOMMANDS:
    set     Set a configuration key-value pair
    get     Get a configuration value by key
    list    List all settings in the config file
    path    Show the path to the configuration file
```

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
