# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.4.0] - 2026-09-25

### Added

- Added an IntelliJ IDEA and Android Studio plugin for Markdown translation, with a progressive preview in the GenTrans tool window.
- Added plugin settings for OpenAI, Google, Anthropic, and Ollama, with API keys stored in the IDE PasswordSafe.
- Added the **Translate Markdown** action in the editor and Project View. It translates a selection when one is active, otherwise the document or selected file.
- Added Markdown-aware CLI translation that preserves Markdown structure and keeps YAML front matter and fenced code blocks verbatim.
- Added `THIRD_PARTY_NOTICES.md` for bundled third-party software notices.

### Changed

- Extended the CLI option set with `-m, --format`; `markdown` selects the Markdown-aware input mode.
- Updated Koog from 0.4.2 to 1.2.0; the Google client uses Koog 1.2.0-beta. HTTP requests now use Koog's Java HTTP client and exclude Ktor.
- Unified the CLI and plugin release version at 0.4.0, and restructured the README for the two distributions.
- Moved the CLI and core packages to `io.github.hiroaki404.gentrans.*`, including the new core translation API. Consumers of the previous packages must update imports.
- Moved CLI configuration resolution into the CLI module. The precedence remains command-line options, local configuration, environment variables, then defaults; the local configuration source is not implemented yet.

### Fixed

- Prevented kotlin-logging's startup message from being written to CLI standard output.

[Unreleased]: https://github.com/hiroaki404/gentrans/compare/v0.4.0...HEAD
[0.4.0]: https://github.com/hiroaki404/gentrans/releases/tag/v0.4.0
