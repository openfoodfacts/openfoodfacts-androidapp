package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class NumberParserUtilsTest {
    private static final float defaultFloat = (float) 1.5;
    private static final int defaultInt = 2;

    @Test
    public void getAsFloat_inIsNull() {
        assertEquals(defaultFloat, NumberParserUtils.getAsFloat(null, defaultFloat), 0.0f);
    }

    @Test
    public void getAsFloat_inIsInt() {
        assertEquals(3.0, NumberParserUtils.getAsFloat(3, defaultFloat), 0.0f);
    }

    @Test
    public void getAsFloat_inIsStringNumber() {
        assertEquals(3.0, NumberParserUtils.getAsFloat("3", defaultFloat), 0.0f);
    }

    @Test
    public void getAsFloat_inIsStringChar() {
        assertEquals(defaultFloat, NumberParserUtils.getAsFloat("a", defaultFloat), 0.0f);
    }

    @Test
    public void getAsFloat_inIsStringBlank() {
        assertEquals(defaultFloat, NumberParserUtils.getAsFloat(" ", defaultFloat), 0.0f);
    }

    @Test
    public void getAsInt_inIsNull() {
        assertEquals(defaultInt, NumberParserUtils.getAsInt(null, defaultInt));
    }

    @Test
    public void getAsInt_inIsFloat() {
        assertEquals(3, NumberParserUtils.getAsInt(3.0, defaultInt));
    }

    @Test
    public void getAsInt_inIsStringNumber() {
        assertEquals(3, NumberParserUtils.getAsInt("3", defaultInt));
    }

    @Test
    public void getAsInt_inIsStringChar() {
        assertEquals(defaultInt, NumberParserUtils.getAsInt("a", defaultInt));
    }

    @Test
    public void getAsInt_inIsStringBlank() {
        assertEquals(defaultInt, NumberParserUtils.getAsInt(" ", defaultInt));
    }
}