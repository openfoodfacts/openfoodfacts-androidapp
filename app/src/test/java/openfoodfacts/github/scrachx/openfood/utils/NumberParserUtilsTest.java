package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class NumberParserUtilsTest {
    private static final float DEFAULT_FLOAT = 1.5f;
    private static final int DEFAULT_INT = 2;

    @Test
    public void getAsFloat_inIsNull() {
        assertThat(NumberParserUtilsKt.getAsFloat(null, DEFAULT_FLOAT)).isWithin(0.0f).of(DEFAULT_FLOAT);
    }

    @Test
    public void getAsFloat_inIsInt() {
        assertThat(NumberParserUtilsKt.getAsFloat(3, DEFAULT_FLOAT)).isWithin(0.0f).of(3.0f);
    }

    @Test
    public void getAsFloat_inIsStringNumber() {
        assertThat(NumberParserUtilsKt.getAsFloat("3", DEFAULT_FLOAT)).isWithin(0.0f).of(3.0f);
    }

    @Test
    public void getAsFloat_inIsStringChar() {
        assertThat(NumberParserUtilsKt.getAsFloat("a", DEFAULT_FLOAT)).isWithin(0.0f).of(DEFAULT_FLOAT);
    }

    @Test
    public void getAsFloat_inIsStringBlank() {
        assertThat(NumberParserUtilsKt.getAsFloat(" ", DEFAULT_FLOAT)).isWithin(0.0f).of(DEFAULT_FLOAT);
    }

    @Test
    public void getAsInt_inIsNull() {
        assertThat(NumberParserUtilsKt.getAsInt(null, DEFAULT_INT)).isEqualTo(DEFAULT_INT);
    }

    @Test
    public void getAsInt_inIsFloat() {
        assertThat(NumberParserUtilsKt.getAsInt(3.0, DEFAULT_INT)).isEqualTo(3);
    }

    @Test
    public void getAsInt_inIsStringNumber() {
        assertThat(NumberParserUtilsKt.getAsInt("3", DEFAULT_INT)).isEqualTo(3);
    }

    @Test
    public void getAsInt_inIsStringChar() {
        assertThat(NumberParserUtilsKt.getAsInt("a", DEFAULT_INT)).isEqualTo(DEFAULT_INT);
    }

    @Test
    public void getAsInt_inIsStringBlank() {
        assertThat(NumberParserUtilsKt.getAsInt(" ", DEFAULT_INT)).isEqualTo(DEFAULT_INT);
    }
}