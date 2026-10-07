package com.alt.otherlives.core.privacy

import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyContractTest {
    @Test
    fun publicPolicyUsesStableHttpsRepositoryUrl() {
        assertTrue(PrivacyPolicy.URL.startsWith("https://"))
        assertTrue(
            PrivacyPolicy.URL ==
                "https://github.com/dbrckk/alt-other-lives/blob/main/PRIVACY.md"
        )
    }
}
