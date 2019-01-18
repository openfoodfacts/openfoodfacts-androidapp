package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.DaoException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static openfoodfacts.github.scrachx.openfood.models.LabelNameTestData.*;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Label}
 */
public class LabelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private DaoSession mockDaoSession;

    @Mock
    private LabelDao mockLabelDao;

    @Mock
    private LabelNameDao mockLabelNameDao;

    private Label mLabel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
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
        thrown.expect(DaoException.class);
        mLabel.getNames();
    }

    @Test
    public void getNamesWithNullNamesAndNonNullDaoSession_getsNamesFromLabelNameDao() {
        mLabel.__setDaoSession(mockDaoSession);
        List<LabelName> labelNames = mLabel.getNames();

        assertEquals(2, labelNames.size());

        LabelName labelName1 = labelNames.get(0);
        assertEquals(LABEL_TAG, labelName1.getLabelTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, labelName1.getLanguageCode());
        assertEquals(LABEL_NAME_EN, labelName1.getName());

        LabelName labelName2 = labelNames.get(1);
        assertEquals(LABEL_TAG, labelName2.getLabelTag());
        assertEquals(LANGUAGE_CODE_FRENCH, labelName2.getLanguageCode());
        assertEquals(LABEL_NAME_FR, labelName2.getName());
    }

    @Test
    public void deleteWithNullDaoSession_throwsDaoException() {
        thrown.expect(DaoException.class);
        mLabel.delete();
    }

    @Test
    public void deleteWithNonNullDaoSession_callsDeleteOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession);
        mLabel.delete();
        verify(mockLabelDao).delete(mLabel);
    }

    @Test
    public void refreshWithNullDaoSession_throwsDaoException() {
        thrown.expect(DaoException.class);
        mLabel.refresh();
    }

    @Test
    public void refreshWithNonNullDaoSession_callsRefreshOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession);
        mLabel.refresh();
        verify(mockLabelDao).refresh(mLabel);
    }

    @Test
    public void updateWithNullDaoSession_throwsDaoException() {
        thrown.expect(DaoException.class);
        mLabel.update();
    }

    @Test
    public void updateWithNonNullDaoSession_callsUpdateOnLabelDao() {
        mLabel.__setDaoSession(mockDaoSession);
        mLabel.update();
        verify(mockLabelDao).update(mLabel);
    }
}
