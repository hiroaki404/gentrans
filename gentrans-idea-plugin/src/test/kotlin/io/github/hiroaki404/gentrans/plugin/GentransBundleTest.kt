package io.github.hiroaki404.gentrans.plugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GentransBundleTest : BasePlatformTestCase() {
    fun testMessageResolvesThePluginDisplayNameKey() {
        assertEquals("GenTrans", GentransBundle.message("gentrans.name"))
    }
}
