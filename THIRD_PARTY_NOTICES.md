# Third-Party Notices

This file was generated from the v0.4.0 CLI and IntelliJ Platform plugin distribution bundles. Regenerate it whenever the bundled dependencies change.

To regenerate, list the JARs in `gentrans-cli/build/distributions/gentrans-*.zip` and `gentrans-idea-plugin/build/distributions/gentrans-idea-plugin-*.zip` with `unzip -l`; exclude the gentrans project JARs; inspect each remaining JAR first with `unzip -Z1` and `unzip -p` for `META-INF/LICENSE*` and `META-INF/NOTICE*`; only when the JAR does not identify a license, read the matching dependency POM's `<licenses>` section from `$GRADLE_USER_HOME/caches/modules-2/files-2.1`. Reconcile the resulting list with both distribution ZIPs before publishing.

The Source column identifies the source used for the license. `JAR license` means the JAR's `META-INF/LICENSE*` supplied it; when shown, `POM URL` identifies the source of the project URL. `POM` means the matching cached POM supplied both fields. For libraries whose JARs carry no license metadata (Clikt, Mordant, colormath, reactive-streams), the license and project URL were taken from the library's POM published on Maven Central for the bundled version (Mordant sub-modules from the `mordant-jvm` 3.0.1 POM, as they are published from the same project). `POM (Maven Central)` marks those rows.

The Apache-2.0 license text is the repository's [LICENSE](LICENSE).

## Bundled in both distributions

| Library | Version | License (SPDX) | Project URL | Source |
| --- | --- | --- | --- | --- |
| ai.koog:agents-core-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:agents-tools-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:agents-utils-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:embeddings-base-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:http-client-core-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:http-client-java | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-executor-anthropic-client-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-executor-clients-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-executor-google-client-jvm | 1.2.0-beta | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-executor-model-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-executor-ollama-client-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-executor-openai-client-base-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-executor-openai-client-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-llm-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-markdown-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-model-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-processor-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-structure-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:prompt-tokenizer-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:serialization-core-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:serialization-jackson | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:utils-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| com.fasterxml.jackson.core:jackson-annotations | 2.21 | Apache-2.0 | https://github.com/FasterXML/jackson-annotations | JAR license; POM URL; JAR NOTICE reproduced below |
| com.fasterxml.jackson.core:jackson-core | 2.21.3 | Apache-2.0 | https://github.com/FasterXML/jackson-core | JAR license; POM URL; JAR NOTICE reproduced below |
| com.fasterxml.jackson.core:jackson-databind | 2.21.3 | Apache-2.0 | https://github.com/FasterXML/jackson-databind | JAR license; POM URL; JAR NOTICE reproduced below |
| com.fasterxml.jackson.module:jackson-module-kotlin | 2.21.3 | Apache-2.0 | https://github.com/FasterXML/jackson-module-kotlin | JAR license; POM URL; JAR NOTICE reproduced below |
| io.github.oshai:kotlin-logging-jvm | 8.0.01 | Apache-2.0 | https://github.com/oshai/kotlin-logging | JAR license; POM URL |
| org.jetbrains:annotations | 26.0.2-1 | Apache-2.0 | https://github.com/JetBrains/java-annotations | POM |
| org.jetbrains:markdown-jvm | 0.7.14 | Apache-2.0 | https://github.com/JetBrains/markdown | POM |
| org.jetbrains.kotlin:kotlin-reflect | 2.3.10 | Apache-2.0 | https://kotlinlang.org/ | POM |
| org.jetbrains.kotlinx:kotlinx-coroutines-jdk9 | 1.10.2 | Apache-2.0 | https://github.com/Kotlin/kotlinx.coroutines | POM |
| org.jetbrains.kotlinx:kotlinx-coroutines-reactive | 1.10.2 | Apache-2.0 | https://github.com/Kotlin/kotlinx.coroutines | POM |
| org.jetbrains.kotlinx:kotlinx-io-bytestring-jvm | 0.9.0 | Apache-2.0 | https://github.com/Kotlin/kotlinx-io | POM |
| org.jetbrains.kotlinx:kotlinx-io-core-jvm | 0.9.0 | Apache-2.0 | https://github.com/Kotlin/kotlinx-io | POM |
| org.jetbrains.kotlinx:kotlinx-schema-annotations-jvm | 0.4.4 | Apache-2.0 | https://github.com/Kotlin/kotlinx-schema | POM |
| org.jetbrains.kotlinx:kotlinx-schema-generator-core-jvm | 0.4.4 | Apache-2.0 | https://github.com/Kotlin/kotlinx-schema | POM |
| org.jetbrains.kotlinx:kotlinx-schema-generator-json-jvm | 0.4.4 | Apache-2.0 | https://github.com/Kotlin/kotlinx-schema | POM |
| org.jetbrains.kotlinx:kotlinx-schema-json-jvm | 0.4.4 | Apache-2.0 | https://github.com/Kotlin/kotlinx-schema | POM |
| org.jetbrains.kotlinx:kotlinx-serialization-core-jvm | 1.10.0 | Apache-2.0 | https://github.com/Kotlin/kotlinx.serialization | POM |
| org.jetbrains.kotlinx:kotlinx-serialization-json-jvm | 1.10.0 | Apache-2.0 | https://github.com/Kotlin/kotlinx.serialization | POM |
| org.reactivestreams:reactive-streams | 1.0.3 | CC0-1.0 | http://www.reactive-streams.org/ | POM (Maven Central) |

## Bundled in the CLI only

The following libraries are bundled in the CLI ZIP. The IntelliJ Platform provides the corresponding Kotlin, coroutine, and SLF4J runtime facilities to the plugin, so they are not bundled in the plugin ZIP.

| Library | Version | License (SPDX) | Project URL | Source |
| --- | --- | --- | --- | --- |
| ai.koog:agents-features-opentelemetry-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| ai.koog:agents-mcp-metadata-jvm | 1.2.0 | Apache-2.0 | https://github.com/JetBrains/koog | POM |
| com.github.ajalt.clikt:clikt-core-jvm | 5.0.3 | Apache-2.0 | https://github.com/ajalt/clikt/ | POM (Maven Central, clikt-jvm 5.0.3) |
| com.github.ajalt.clikt:clikt-jvm | 5.0.3 | Apache-2.0 | https://github.com/ajalt/clikt/ | POM (Maven Central) |
| com.github.ajalt.colormath:colormath-jvm | 3.6.0 | MIT | https://github.com/ajalt/colormath/ | POM (Maven Central) |
| com.github.ajalt.mordant:mordant-jvm-ffm-jvm | 3.0.1 | Apache-2.0 | https://github.com/ajalt/mordant/ | POM (Maven Central, mordant-jvm 3.0.1; same project) |
| com.github.ajalt.mordant:mordant-jvm-graal-ffi-jvm | 3.0.1 | Apache-2.0 | https://github.com/ajalt/mordant/ | POM (Maven Central, mordant-jvm 3.0.1; same project) |
| com.github.ajalt.mordant:mordant-jvm-jna-jvm | 3.0.1 | Apache-2.0 | https://github.com/ajalt/mordant/ | POM (Maven Central, mordant-jvm 3.0.1; same project) |
| com.github.ajalt.mordant:mordant-core-jvm | 3.0.1 | Apache-2.0 | https://github.com/ajalt/mordant/ | POM (Maven Central, mordant-jvm 3.0.1; same project) |
| com.github.ajalt.mordant:mordant-jvm | 3.0.1 | Apache-2.0 | https://github.com/ajalt/mordant/ | POM (Maven Central) |
| io.opentelemetry.kotlin:api-ext-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:api-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:compat-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:core-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:exporters-core-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:implementation-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:java-typealiases-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:model-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:noop-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:platform-implementations-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:sdk-api-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:sdk-common-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry.kotlin:semconv-jvm | 0.3.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-kotlin | POM |
| io.opentelemetry:opentelemetry-api | 1.61.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-java | POM |
| io.opentelemetry:opentelemetry-common | 1.61.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-java | POM |
| io.opentelemetry:opentelemetry-context | 1.61.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-java | POM |
| io.opentelemetry:opentelemetry-sdk | 1.61.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-java | POM |
| io.opentelemetry:opentelemetry-sdk-common | 1.61.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-java | POM |
| io.opentelemetry:opentelemetry-sdk-logs | 1.61.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-java | POM |
| io.opentelemetry:opentelemetry-sdk-metrics | 1.61.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-java | POM |
| io.opentelemetry:opentelemetry-sdk-trace | 1.61.0 | Apache-2.0 | https://github.com/open-telemetry/opentelemetry-java | POM |
| net.java.dev.jna:jna | 5.14.0 | Apache-2.0 (selected from Apache-2.0 OR LGPL-2.1) | https://github.com/java-native-access/jna | JAR license; POM URL (Maven Central) |
| org.jetbrains.kotlin:kotlin-stdlib | 2.3.10 | Apache-2.0 | https://kotlinlang.org/ | POM |
| org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm | 1.10.2 | Apache-2.0 | https://github.com/Kotlin/kotlinx.coroutines | POM |
| org.slf4j:slf4j-api | 2.0.17 | MIT | http://www.slf4j.org | JAR license; POM URL |
| org.slf4j:slf4j-simple | 2.0.17 | MIT | http://www.slf4j.org | JAR license; POM URL |

## Embedded notices from jackson-core

`jackson-core` bundles shaded FastDoubleParser and Schubfach code. Their license texts below are reproduced from its `META-INF/FastDoubleParser-LICENSE`, `META-INF/FastDoubleParser-ThirdParty-LICENSE`, and `META-INF/Schubfach-LICENSE` files.

| Embedded component | License (SPDX) |
| --- | --- |
| FastDoubleParser | MIT |
| fast_float | MIT |
| bigint | BSD-2-Clause |
| Schubfach | MIT |
| fast_double_parser | BSL-1.0 |

## Apache notices

### Jackson annotations, core, and databind

```
# Jackson JSON processor

Jackson is a high-performance, Free/Open Source JSON processing library.
It was originally written by Tatu Saloranta (tatu.saloranta@iki.fi), and has
been in development since 2007.
It is currently developed by a community of developers.

## Copyright

Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)

## Licensing

Jackson 2.x core and extension components are licensed under Apache License 2.0
To find the details that apply to this artifact see the accompanying LICENSE file.

## Credits

A list of contributors may be found from CREDITS(-2.x) file, which is included
in some artifacts (usually source distributions); but is always available
from the source code management (SCM) system project uses.
```

### Jackson core additions

```
## FastDoubleParser

jackson-core bundles a shaded copy of FastDoubleParser <https://github.com/wrandelshofer/FastDoubleParser>.
That code is available under an MIT license <https://github.com/wrandelshofer/FastDoubleParser/blob/main/LICENSE>
under the following copyright.

Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.

See FastDoubleParser-LICENSE and also FastDoubleParser-ThirdParty-LICENSE for details of other source code
included in FastDoubleParser and the licenses and copyrights that apply to that code.

## Schubfach

jackson-core bundles a copy of the Schubfach number writing code <https://github.com/c4f7fcce9cb06515/Schubfach>.
That code is available under an MIT license <https://github.com/c4f7fcce9cb06515/Schubfach/blob/master/todec/LICENSE>
under the following copyright.

Copyright 2018-2020 Raffaello Giulietti

See Schubfach-LICENSE.
```

### Jackson module Kotlin

```
# Jackson JSON processor

Jackson is a high-performance, Free/Open Source JSON processing library.
It was originally written by Tatu Saloranta (tatu.saloranta@iki.fi), and has
been in development since 2007.
It is currently developed by a community of developers, as well as supported
commercially by FasterXML.com.

## Copyright

Copyright 2007-, Tatu Saloranta (tatu.saloranta@iki.fi)

## Licensing

Jackson core and extension components may be licensed under different licenses.
To find the details that apply to this artifact see the accompanying LICENSE file.
For more information, including possible other licensing options, contact
FasterXML.com (http://fasterxml.com).

## Credits

A list of contributors may be found from CREDITS file, which is included
in some artifacts (usually source distributions); but is always available
from the source code management (SCM) system project uses.
```

## Full non-Apache license texts

### MIT License

Copyright (c) 2004-2022 QOS.ch Sarl (Switzerland)

All rights reserved.

Copyright (c) 2024 Werner Randelshofer, Switzerland.

Copyright (c) 2021 The fast_float authors

Copyright 2018-2020 Raffaello Giulietti

Copyright 2021 AJ Alt

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

### Boost Software License 1.0

Copyright (c) 2022 Daniel Lemire

Copyright (c) Daniel Lemire

Boost Software License - Version 1.0 - August 17th, 2003

Permission is hereby granted, free of charge, to any person or organization
obtaining a copy of the software and accompanying documentation covered by
this license (the "Software") to use, reproduce, display, distribute,
execute, and transmit the Software, and to prepare derivative works of the
Software, and to permit third-parties to whom the Software is furnished to
do so, all subject to the following:

The copyright notices in the Software and this entire statement, including
the above license grant, this restriction and the following disclaimer,
must be included in all copies of the Software, in whole or in part, and
all derivative works of the Software, unless such copies or derivative
works are solely in the form of machine-executable object code generated by
a source language processor.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.

### BSD 2-Clause License

Copyright 2020 Tim Buktu

Copyright 2022 Tim Buktu

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
