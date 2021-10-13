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
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class AnalysisTagTest {
    private lateinit var testAnalysisTag: AnalysisTag
    private val tagEnglish = AnalysisTagName(UNIQUE_ALLERGEN_ID_2, LANGUAGE_CODE_ENGLISH, PEANUTS_EN, "show")
    private val tagGerman = AnalysisTagName(UNIQUE_ALLERGEN_ID_1, LANGUAGE_CODE_GERMAN, PEANUTS_DE, "show")
    private lateinit var tagNames: MutableList<AnalysisTagName>

    @Mock
    var mockDaoSession: DaoSession? = null

    @Mock
    var mockAnalysisTagNameDao: AnalysisTagNameDao? = null

    @Before
    fun setUp() {
        tagNames = arrayListOf(tagGerman, tagEnglish)

        whenever(mockDaoSession!!.analysisTagNameDao) doReturn mockAnalysisTagNameDao
        whenever(mockAnalysisTagNameDao!!._queryAnalysisTag_Names(Mockito.any())) doReturn tagNames

        testAnalysisTag = AnalysisTag()
    }

    @Test
    fun getNames_DaoSessionIsNull() {
        assertThrows(DaoException::class.java) { testAnalysisTag.names }
    }

    @Test
    fun getNames_returnsListOfTags() {
        testAnalysisTag.__setDaoSession(mockDaoSession)
        val tags = testAnalysisTag.names
        assertThat(tags[0].analysisTag).isEqualTo(UNIQUE_ALLERGEN_ID_1)
        assertThat(tags[0].languageCode).isEqualTo(LANGUAGE_CODE_GERMAN)
        assertThat(tags[0].name).isEqualTo(PEANUTS_DE)
        assertThat(tags[1].analysisTag).isEqualTo(UNIQUE_ALLERGEN_ID_2)
        assertThat(tags[1].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(tags[1].name).isEqualTo(PEANUTS_EN)
    }

    @Test(expected = DaoException::class)
    @Throws(DaoException::class)
    fun delete_throwsExceptionMyDaoIsNull() {
        testAnalysisTag.__setDaoSession(mockDaoSession)
        testAnalysisTag.delete()
    }
}