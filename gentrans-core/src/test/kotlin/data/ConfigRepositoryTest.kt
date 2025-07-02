package data

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import model.EnvConfigs
import model.LocalConfigs

class ConfigRepositoryTest : DescribeSpec({
    describe("ConfigRepository") {
        describe("getConfigs") {
            context("when both data sources return configs") {
                it("should return list containing configs from both data sources") {
                    // given
                    val envConfigs = EnvConfigs(
                        llmModelKey = "env-model-key",
                        providerKey = "env-provider-key"
                    )
                    val localConfigs = LocalConfigs(
                        llmModelKey = "local-model-key",
                        providerKey = "local-provider-key"
                    )

                    val fakeEnvConfigDataSource = FakeConfigDataSource(envConfigs)
                    val fakeLocalConfigDataSource = FakeConfigDataSource(localConfigs)
                    val configRepository = ConfigRepository(fakeEnvConfigDataSource, fakeLocalConfigDataSource)

                    // when
                    val result = configRepository.getConfigs()

                    // then
                    result shouldBe listOf(envConfigs, localConfigs)
                    result.size shouldBe 2
                }
            }
        }
    }
})
