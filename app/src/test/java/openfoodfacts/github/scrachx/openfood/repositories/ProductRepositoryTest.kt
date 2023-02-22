package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.utils.InstallationService
import openfoodfacts.github.scrachx.openfood.utils.getVersionName
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProductRepositoryTest {

    private lateinit var mockContext: Context
    private lateinit var mockInstallationService: InstallationService

    @BeforeEach
    fun beforeAll() {
        mockkStatic("openfoodfacts.github.scrachx.openfood.utils.ContextKt")
        mockInstallationService = mockk {
            every { id } returns "FAKE_ID"
        }
        mockContext = mockk {
            every { getVersionName() } returns "TEST_VERSION_NAME"
        }
    }

    @Test
    fun `comment to upload with username`() {
        // Logged in test
        val actual = ProductRepository.getCommentToUpload(
            mockContext,
            mockInstallationService,
            "USERNAME"
        )
        val expected = "Official ${BuildConfig.APP_NAME} Android app TEST_VERSION_NAME"
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `comment to upload without username`() {
        val actual = ProductRepository.getCommentToUpload(
            mockContext,
            mockInstallationService,
            null
        )
        val expected = "Official ${BuildConfig.APP_NAME} Android app TEST_VERSION_NAME (Added by FAKE_ID)"
        assertThat(actual).isEqualTo(expected)
    }
}