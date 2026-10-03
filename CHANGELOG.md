# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Added per-file saved translations to the plugin. The latest completed translation of each file is shown again when you switch back to it, including after restarting the IDE.
- Added notices in the preview when the source file has changed since the translation, and when the translation covers only a selection. The changed-source notice updates about one second after you stop editing.
- Added concurrent translations of different files, up to three at the same time. Switching files no longer cancels a running translation.
- Added a **Clear translation cache** button to **Settings > Tools > GenTrans**.

### Changed

- **Copy** and **Save as** are available once a translation has completed, and act on the file whose translation is shown. Copying partial text during a translation is no longer possible.
- **Re-run** translates the whole current text of the displayed file, even if the previous run translated a selection.
- **Cancel** stops only the translation of the displayed file.

## [0.4.0] - 2026-09-26

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

[Unreleased]: https://github.com/hiroaki404/gentrans/compare/0.4.0...HEAD
[0.4.0]: https://github.com/hiroaki404/gentrans/releases/tag/0.4.0
