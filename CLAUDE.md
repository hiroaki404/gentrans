# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`gentrans` is an AI-powered command-line translation tool built with Kotlin. It supports multiple AI providers (OpenAI, Google, Anthropic, Meta, Alibaba, OpenRouter, Ollama) and follows clean architecture principles.

## Technology Stack

- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL
- **CLI Framework**: Clikt
- **AI Library**: Koog Agents
- **Testing**: Kotest + JUnit5
- **Architecture**: Clean Architecture with dependency injection

## Development Commands

### Building and Running
```bash
# Build the project
./gradlew build

# Create executable distribution (preferred for verification)
./gradlew installDist

# Run from distribution
./build/install/gentrans/bin/gentrans --version
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with specific pattern
./gradlew test --tests "*UseCaseTest"
```

### Development Guidelines
- Use `./gradlew installDist` when checking builds
- Follow Test-Driven Development (TDD) using t-wada methodology
- Commit messages: short, simple, in English
- Use `git add` for all changes when committing
- Only commit when explicitly requested
- Refer to README.md for specifications
- For Clikt implementation reference: https://ajalt.github.io/clikt/

## Project Architecture

The codebase follows **Clean Architecture** with clear separation of concerns:

- **Presentation Layer**: `Main.kt` (Clikt-based CLI commands)
- **Domain Layer**: `domain/` (Use cases for AI client and model configuration)
- **Data Layer**: `data/` (Configuration data sources with priority hierarchy)
- **Core Module**: `gentrans-core/` (Business logic separated from CLI)

### Configuration Priority System
Settings are resolved in this order (highest to lowest priority):
1. Command-line options
2. Local configuration files
3. Environment variables
4. Default values

### Key Components
- `GetExecutorUseCase`: Manages AI client creation and provider selection
- `GetLLModelUseCase`: Handles model configuration
- `ConfigDataSource` implementations: Environment, local, and option-based configuration

## Testing Approach

- **Framework**: Kotest with StringSpec and FunSpec, etc.
- **Strategy**: Test-driven development following t-wada methodology
- **Mocking**: Uses Koog Agents testing tools
- **Coverage**: Focus on use cases and CLI interface integration

## Code Conventions

- **Dependency Injection**: Constructor-based DI pattern
- **Functional Programming**: Operator overloading with `invoke` functions
- **Error Handling**: Meaningful exception messages with proper context
- **Immutable Data**: Data classes for configuration models

## Environment Setup

Required environment variables for development:
- `GENTRANS_API_KEY`: API key for AI providers
- `GENTRANS_PROVIDER`: Default AI provider (openai, google, etc.)
- `GENTRANS_MODEL`: Default model name

## Supported AI Providers and Models

Verified combinations:
- **OpenAI**: `gpt-4o`, `gpt-4o-mini`, `o3`
- **Google**: `gemini-2.0-flash`
- **Anthropic**: `claude-3-opus`, `claude-sonnet-4-0`
- **Ollama**: `llama3.2`

Model support depends on the Koog library - refer to their documentation for available models per provider.
