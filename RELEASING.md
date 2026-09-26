# Releasing gentrans

Both distributions use the version in `gradle/libs.versions.toml` and are published together in one GitHub Release.

## 1. Prepare the release

1. Set `gentrans` in `gradle/libs.versions.toml` to `<VERSION>`.
2. Move the intended changes from `Unreleased` to a dated `<VERSION>` section in `CHANGELOG.md`, and update its comparison and release links.
3. When dependencies changed, regenerate `THIRD_PARTY_NOTICES.md` by following the procedure at the start of that file before continuing.
4. Commit these changes and merge them into `main`. The release tag in step 5 must point at the commit on `main` that contains them.

The shell snippets in this document assume bash or zsh. In fish, replace `VERSION=X.Y.Z` with `set VERSION X.Y.Z` and run the multi-line commands as-is.

## 2. Run the release checks

```bash
CI=true ./gradlew ktlintCheck test
./gradlew :gentrans-core:smokeTest
./gradlew :gentrans-idea-plugin:verifyPlugin
```

`CI=true` excludes the CLI's `integration` tests; core `smokeTest` is always excluded from regular `test` and CI. The smoke test performs real HTTP checks and needs network access but no API key. `verifyPlugin` uses the locally installed Android Studio and IntelliJ IDEA by default. On another machine, override their locations with `-Pgentrans.localIdePath="/path/to/Android Studio.app"` and `-Pgentrans.localIdeaPath="/path/to/IntelliJ IDEA.app"`.

Before merging a Dependabot PR that updates Koog in `gradle/libs.versions.toml`, run `:gentrans-idea-plugin:verifyPlugin` and `:gentrans-core:smokeTest` locally. The core and CLI tests run by CI do not determine IDE compatibility or exercise the real HTTP smoke checks.

## 3. Build and inspect the release archives

```bash
./gradlew :gentrans-cli:distZip :gentrans-cli:shadowDistZip
./gradlew :gentrans-idea-plugin:buildPlugin
unzip -l gentrans-cli/build/distributions/gentrans-<VERSION>.zip | grep -E 'LICENSE|THIRD_PARTY_NOTICES\.md'
unzip -l gentrans-cli/build/distributions/gentrans-cli-shadow-<VERSION>.zip | grep -E 'LICENSE|THIRD_PARTY_NOTICES\.md'
PLUGIN_JAR="$(mktemp)"
unzip -p gentrans-idea-plugin/build/distributions/gentrans-idea-plugin-<VERSION>.zip "gentrans-idea-plugin/lib/gentrans-idea-plugin-<VERSION>.jar" > "$PLUGIN_JAR"
unzip -l "$PLUGIN_JAR" | grep -E 'META-INF/(LICENSE|THIRD_PARTY_NOTICES\.md)'
rm "$PLUGIN_JAR"
```

The standard CLI archive is `gentrans-cli/build/distributions/gentrans-<VERSION>.zip`, which matches the CLI Installation instructions in `README.md`. The Shadow archive is `gentrans-cli/build/distributions/gentrans-cli-shadow-<VERSION>.zip`; inspect it but do not attach it to the GitHub Release. The plugin archive is `gentrans-idea-plugin/build/distributions/gentrans-idea-plugin-<VERSION>.zip`, whose main JAR contains `META-INF/LICENSE` and `META-INF/THIRD_PARTY_NOTICES.md`. Confirm the legal files in all three inspected artifacts.

## 4. Perform manual checks

In a clean Android Studio or IntelliJ IDEA profile, open **Settings > Plugins > Install Plugin from Disk...**, select the plugin ZIP, and translate Markdown from the GenTrans tool window. Extract the standard CLI ZIP, add its `bin` directory to `PATH`, and run the CLI checks:

```bash
export PATH="$PWD/gentrans-<VERSION>/bin:$PATH"
gentrans --version
export GENTRANS_API_KEY="your-api-key"
gentrans "hello"
```

## 5. Tag, push, and create the GitHub Release

```bash
VERSION=X.Y.Z # Replace with the actual release version
awk "/^## \\[$VERSION\\]/{release=1; next} release && (/^## / || /^\[/){exit} release" CHANGELOG.md > "/tmp/gentrans-$VERSION-release-notes.md"
git tag "$VERSION"
git push origin "$VERSION"
gh release create "$VERSION" \
  "gentrans-cli/build/distributions/gentrans-$VERSION.zip" \
  "gentrans-idea-plugin/build/distributions/gentrans-idea-plugin-$VERSION.zip" \
  --title "$VERSION" --notes-file "/tmp/gentrans-$VERSION-release-notes.md"
```

Tags and release names carry no `v` prefix (`0.4.0`), matching the earlier releases. Before running `gh release create`, obtain explicit confirmation from the user. Attach only the standard CLI ZIP and the plugin ZIP.

## 6. Prepare for the next development cycle

Restore an empty `Unreleased` section at the top of `CHANGELOG.md` and update its comparison link for the next development cycle.
