package openfoodfacts.github.scrachx.openfood.models.entities.label;

import org.greenrobot.greendao.DaoException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.DaoSession;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_FR;
import static openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_TAG;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Label}
 */
@RunWith(MockitoJUnitRunner.class)
public class LabelTest {
    @Mock
    private DaoSession mockDaoSession;
    @Mock
    private LabelDao mockLabelDao;
    @Mock
    private LabelNameDao mockLabelNameDao;
    private Label mLabel;

    @Before
    public void setup() {
        when(mockDaoSession.getLabelDao()).thenReturn(mockLabelDao);
        when(mockDaoSession.getLabelNameDao()).thenReturn(mockLabelNameDao);
        LabelName labelName1 = new LabelName(LABEL_TAG, LANGUAGE_CODE_ENGLISH, LABEL_NAME_EN);
        LabelName labelName2 = new LabelName(LABEL_TAG, LANGUAGE_CODE_FRENCH, LABEL_NAME_FR);
        when(mockLabelNameDao._queryLabel_Names(any()))
            .thenReturn(Arrays.asList(labelName1, labelName2));
        mLabel = new Label();
    }

    @Test
    public void getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mLabel.getNames());
    }

    @Test
    public void getNamesWithNullNamesAndNonNullDaoSession_getsNamesFromLabelNameDao() {
        mLabel.__setDaoSession(mockDaoSession);
        List<LabelName> labelNames = mLabel.getNames();

        assertThat(labelNames).hasSize(2);

        LabelName labelName1 = labelNames.get(0);
        assertThat(labelName1.getLabelTag()).isEqualTo(LABEL_TAG);
        assertThat(labelName1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(labelName1.getName()).isEqualTo(LABEL_NAME_EN);

        LabelName labelName2 = labelNames.get(1);
        assertThat(labelName2.getLabelTag()).isEqualTo(LABEL_TAG);
        assertThat(labelName2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(labelName2.getName()).isEqualTo(LABEL_NAME_FR);
    }

    @Test
    public void deleteWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mLabel.delete());
    }

    @Test
    public void deleteWithNonNullDaoSession_callsDeleteOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession);
        mLabel.delete();
        verify(mockLabelDao).delete(mLabel);
    }

    @Test
    public void refreshWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mLabel.refresh());
    }

    @Test
    public void refreshWithNonNullDaoSession_callsRefreshOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession);
        mLabel.refresh();
        verify(mockLabelDao).refresh(mLabel);
    }

    @Test
    public void updateWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mLabel.update());
    }

    @Test
    public void updateWithNonNullDaoSession_callsUpdateOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession);
        mLabel.update();
        verify(mockLabelDao).update(mLabel);
    }

    @Test
    public void resetNames_callsGetLabelNameDao() {
        mLabel.__setDaoSession(mockDaoSession);
        mLabel.resetNames();
        mLabel.getNames();
        verify(mockDaoSession).getLabelNameDao();
    }
}
