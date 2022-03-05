package openfoodfacts.github.scrachx.openfood.models.entities.label

import com.google.common.truth.Truth
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_EN
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_FR
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_TAG
import org.greenrobot.greendao.DaoException
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

/**
 * Tests for [Label]
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LabelTest {
    @Mock
    private lateinit var mockDaoSession: DaoSession

    @Mock
    private lateinit var mockLabelDao: LabelDao

    @Mock
    private val mockLabelNameDao: LabelNameDao? = null

    private lateinit var mLabel: Label

    @BeforeEach
    fun setup() {
        whenever(mockDaoSession.labelDao) doReturn mockLabelDao
        whenever(mockDaoSession.labelNameDao) doReturn mockLabelNameDao
        val labelName1 = LabelName(LABEL_TAG, LANGUAGE_CODE_ENGLISH, LABEL_NAME_EN)
        val labelName2 = LabelName(LABEL_TAG, LANGUAGE_CODE_FRENCH, LABEL_NAME_FR)
        whenever(mockLabelNameDao!!._queryLabel_Names(ArgumentMatchers.any())) doReturn listOf(labelName1, labelName2)
        mLabel = Label()
    }

    @Test
    fun getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        Assert.assertThrows(DaoException::class.java) { mLabel.names }
    }

    @Test
    fun getNamesWithNullNamesAndNonNullDaoSession_getsNamesFromLabelNameDao() {
        mLabel.__setDaoSession(mockDaoSession)
        val labelNames = mLabel.names
        Truth.assertThat(labelNames).hasSize(2)
        val labelName1 = labelNames[0]
        Truth.assertThat(labelName1!!.labelTag).isEqualTo(LABEL_TAG)
        Truth.assertThat(labelName1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        Truth.assertThat(labelName1.name).isEqualTo(LABEL_NAME_EN)
        val labelName2 = labelNames[1]
        Truth.assertThat(labelName2!!.labelTag).isEqualTo(LABEL_TAG)
        Truth.assertThat(labelName2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        Truth.assertThat(labelName2.name).isEqualTo(LABEL_NAME_FR)
    }

    @Test
    fun deleteWithNullDaoSession_throwsDaoException() {
        Assert.assertThrows(DaoException::class.java) { mLabel.delete() }
    }

    @Test
    fun deleteWithNonNullDaoSession_callsDeleteOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession)
        mLabel.delete()
        verify(mockLabelDao)!!.delete(mLabel)
    }

    @Test
    fun refreshWithNullDaoSession_throwsDaoException() {
        Assert.assertThrows(DaoException::class.java) { mLabel.refresh() }
    }

    @Test
    fun refreshWithNonNullDaoSession_callsRefreshOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession)
        mLabel.refresh()
        verify(mockLabelDao)!!.refresh(mLabel)
    }

    @Test
    fun updateWithNullDaoSession_throwsDaoException() {
        Assert.assertThrows(DaoException::class.java) { mLabel.update() }
    }

    @Test
    fun updateWithNonNullDaoSession_callsUpdateOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession)
        mLabel.update()
        verify(mockLabelDao)!!.update(mLabel)
    }

    @Test
    fun resetNames_callsGetLabelNameDao() {
        mLabel.__setDaoSession(mockDaoSession)
        mLabel.resetNames()
        mLabel.names
        verify(mockDaoSession)!!.labelNameDao
    }
}