package openfoodfacts.github.scrachx.openfood.models.entities.analysistag

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_GERMAN
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.PEANUTS_DE
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.PEANUTS_EN
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.UNIQUE_ALLERGEN_ID_1
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.UNIQUE_ALLERGEN_ID_2
import org.greenrobot.greendao.DaoException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class AnalysisTagTest {
    private lateinit var testAnalysisTag: AnalysisTag
    private val tagEnglish = AnalysisTagName(
        /* allergenTag = */ UNIQUE_ALLERGEN_ID_2,
        /* languageCode = */ LANGUAGE_CODE_ENGLISH,
        /* name = */ PEANUTS_EN,
        /* showIngredients = */ "show"
    )
    private val tagGerman = AnalysisTagName(
        /* allergenTag = */ UNIQUE_ALLERGEN_ID_1,
        /* languageCode = */ LANGUAGE_CODE_GERMAN,
        /* name = */ PEANUTS_DE,
        /* showIngredients = */ "show"
    )
    private lateinit var tagNames: MutableList<AnalysisTagName>

    @Mock
    lateinit var mockDaoSession: DaoSession

    @Mock
    lateinit var mockAnalysisTagNameDao: AnalysisTagNameDao

    @BeforeEach
    fun setUp() {
        tagNames = arrayListOf(tagGerman, tagEnglish)
        testAnalysisTag = AnalysisTag()
    }

    @Test
    fun getNames_DaoSessionIsNull() {
        assertThrows<DaoException> { testAnalysisTag.names }
    }

    @Test
    fun getNames_returnsListOfTags() {
        whenever(mockDaoSession.analysisTagNameDao) doReturn mockAnalysisTagNameDao
        whenever(mockAnalysisTagNameDao._queryAnalysisTag_Names(Mockito.any())) doReturn tagNames

        testAnalysisTag.__setDaoSession(mockDaoSession)
        val tags = testAnalysisTag.names
        assertThat(tags[0].analysisTag).isEqualTo(UNIQUE_ALLERGEN_ID_1)
        assertThat(tags[0].languageCode).isEqualTo(LANGUAGE_CODE_GERMAN)
        assertThat(tags[0].name).isEqualTo(PEANUTS_DE)
        assertThat(tags[1].analysisTag).isEqualTo(UNIQUE_ALLERGEN_ID_2)
        assertThat(tags[1].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(tags[1].name).isEqualTo(PEANUTS_EN)
    }

    @Test
    fun delete_throwsExceptionMyDaoIsNull() {
        assertThrows<DaoException> {
            testAnalysisTag.__setDaoSession(mockDaoSession)
            testAnalysisTag.delete()
        }
    }
}