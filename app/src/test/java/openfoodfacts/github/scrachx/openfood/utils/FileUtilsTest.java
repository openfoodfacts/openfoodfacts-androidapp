package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class FileUtilsTest {
    private final String absoluteURL = "/path";
    private final String localURL = "file://path";

    @Test
    public void fileIsLocal_true() {
        assertThat(FileUtils.isLocaleFile(localURL)).isTrue();
    }

    @Test
    public void fileIsLocal_false() {
        assertThat(FileUtils.isLocaleFile(absoluteURL)).isFalse();
    }

    @Test
    public void isAbsolute_true() {
        assertThat(FileUtils.isAbsolute(absoluteURL)).isTrue();
    }

    @Test
    public void isAbsolute_false() {
        assertThat(FileUtils.isAbsolute(localURL)).isFalse();
    }
}