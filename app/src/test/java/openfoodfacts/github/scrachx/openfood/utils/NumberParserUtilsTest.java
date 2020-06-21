package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class NumberParserUtilsTest {
    float defaultFloat = (float)1.5;
    int defaultInt = 2;
    @Test
    public void getAsFloat_inIsNull(){
        assertEquals(NumberParserUtils.getAsFloat(null, defaultFloat), defaultFloat, 0.0f);
    }

    @Test
    public void getAsFloat_inIsInt(){
        assertEquals(NumberParserUtils.getAsFloat(3, defaultFloat), 3.0, 0.0f);
    }

    @Test
    public void getAsFloat_inIsStringNumber(){
        assertEquals(NumberParserUtils.getAsFloat("3", defaultFloat), 3.0, 0.0f);
    }

    @Test
    public void getAsFloat_inIsStringChar(){
        assertEquals(NumberParserUtils.getAsFloat("a", defaultFloat), defaultFloat, 0.0f);
    }

    @Test
    public void getAsFloat_inIsStringBlank(){
        assertEquals(NumberParserUtils.getAsFloat(" ", defaultFloat), defaultFloat, 0.0f);
    }

    @Test
    public void getAsInt_inIsNull(){
        assertEquals(NumberParserUtils.getAsInt(null, defaultInt), defaultInt);
    }

    @Test
    public void getAsInt_inIsFloat(){
        assertEquals(NumberParserUtils.getAsInt(3.0, defaultInt), 3);
    }

    @Test
    public void getAsInt_inIsStringNumber(){
        assertEquals(NumberParserUtils.getAsInt("3", defaultInt), 3);
    }

    @Test
    public void getAsInt_inIsStringChar(){
        assertEquals(NumberParserUtils.getAsInt("a", defaultInt), defaultInt);
    }

    @Test
    public void getAsInt_inIsStringBlank(){
        assertEquals(NumberParserUtils.getAsInt(" ", defaultInt), defaultInt);
    }
}