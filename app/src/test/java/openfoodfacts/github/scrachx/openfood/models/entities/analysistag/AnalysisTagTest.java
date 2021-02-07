package openfoodfacts.github.scrachx.openfood.models.entities.analysistag;

import org.greenrobot.greendao.DaoException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.DaoSession;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_GERMAN;
import static openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenResponseTestData.PEANUTS_DE;
import static openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenResponseTestData.PEANUTS_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenResponseTestData.UNIQUE_ALLERGEN_ID_1;
import static openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenResponseTestData.UNIQUE_ALLERGEN_ID_2;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisTagTest {
    private AnalysisTag testAnalysisTag;
    private final AnalysisTagName tagEnglish = new AnalysisTagName(UNIQUE_ALLERGEN_ID_2, LANGUAGE_CODE_ENGLISH, PEANUTS_EN, "show");
    private final AnalysisTagName tagGerman = new AnalysisTagName(UNIQUE_ALLERGEN_ID_1, LANGUAGE_CODE_GERMAN, PEANUTS_DE, "show");
    private List<AnalysisTagName> tagNames;
    @Mock
    DaoSession mockDaoSession;
    @Mock
    AnalysisTagNameDao mockAnalysisTagNameDao;

    @Before
    public void setUp() {
        tagNames = new ArrayList<>();
        tagNames.add(tagGerman);
        tagNames.add(tagEnglish);

        when(mockDaoSession.getAnalysisTagNameDao()).thenReturn(mockAnalysisTagNameDao);
        when(mockAnalysisTagNameDao._queryAnalysisTag_Names(Mockito.any())).thenReturn(tagNames);

        testAnalysisTag = new AnalysisTag();
    }

    @Test
    public void getNames_DaoSessionIsNull() {
        assertThrows(DaoException.class, () -> testAnalysisTag.getNames());
    }

    @Test
    public void getNames_returnsListOfTags() {
        testAnalysisTag.__setDaoSession(mockDaoSession);

        List<AnalysisTagName> tags = testAnalysisTag.getNames();

        assertThat(tags.get(0).getAnalysisTag()).isEqualTo(UNIQUE_ALLERGEN_ID_1);
        assertThat(tags.get(0).getLanguageCode()).isEqualTo(LANGUAGE_CODE_GERMAN);
        assertThat(tags.get(0).getName()).isEqualTo(PEANUTS_DE);

        assertThat(tags.get(1).getAnalysisTag()).isEqualTo(UNIQUE_ALLERGEN_ID_2);
        assertThat(tags.get(1).getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(tags.get(1).getName()).isEqualTo(PEANUTS_EN);
    }

    @Test(expected = DaoException.class)
    public void delete_throwsExceptionMyDaoIsNull() throws DaoException {
        testAnalysisTag.__setDaoSession(mockDaoSession);
        testAnalysisTag.delete();
    }
}