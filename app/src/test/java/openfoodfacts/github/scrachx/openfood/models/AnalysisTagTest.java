package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.DaoException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static openfoodfacts.github.scrachx.openfood.models.AllergenResponseTestData.UNIQUE_ALLERGEN_ID_1;
import static openfoodfacts.github.scrachx.openfood.models.AllergenResponseTestData.UNIQUE_ALLERGEN_ID_2;
import static openfoodfacts.github.scrachx.openfood.models.AllergenResponseTestData.PEANUTS_DE;
import static openfoodfacts.github.scrachx.openfood.models.AllergenResponseTestData.PEANUTS_EN;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_GERMAN;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class AnalysisTagTest {

    private AnalysisTag testAnalysisTag;
    private final AnalysisTagName tagGerman = new AnalysisTagName(UNIQUE_ALLERGEN_ID_1,  LANGUAGE_CODE_GERMAN, PEANUTS_DE, "show");
    private final AnalysisTagName tagEnglish = new AnalysisTagName(UNIQUE_ALLERGEN_ID_2,  LANGUAGE_CODE_ENGLISH, PEANUTS_EN, "show");
    private List<AnalysisTagName> tagNames;

    @Mock
    DaoSession mockDaoSession;

    @Mock
    AnalysisTagNameDao mockAnalysisTagNameDao;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp(){
        tagNames = new ArrayList<>();
        tagNames.add(tagGerman);
        tagNames.add(tagEnglish);

        MockitoAnnotations.initMocks(this);
        when(mockDaoSession.getAnalysisTagNameDao()).thenReturn(mockAnalysisTagNameDao);
        when(mockAnalysisTagNameDao._queryAnalysisTag_Names(Mockito.any())).thenReturn(tagNames);

        testAnalysisTag = new AnalysisTag();
    }

    @Test
    public void getNames_DaoSessionIsNull() throws DaoException{
        thrown.expect(DaoException.class);
        testAnalysisTag.getNames();
    }


    @Test
    public void getNames_returnsListOfTags(){
        testAnalysisTag.__setDaoSession(mockDaoSession);

        List<AnalysisTagName> tags = testAnalysisTag.getNames();

        assertEquals(UNIQUE_ALLERGEN_ID_1, tags.get(0).getAnalysisTag());
        assertEquals(LANGUAGE_CODE_GERMAN, tags.get(0).getLanguageCode());
        assertEquals(PEANUTS_DE, tags.get(0).getName());

        assertEquals(UNIQUE_ALLERGEN_ID_2, tags.get(1).getAnalysisTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, tags.get(1).getLanguageCode());
        assertEquals(PEANUTS_EN, tags.get(1).getName());

    }

    @Test (expected = DaoException.class)
    public void delete_throwsExceptionMyDaoIsNull() throws DaoException {
        testAnalysisTag.__setDaoSession(mockDaoSession);
        testAnalysisTag.delete();
    }
}