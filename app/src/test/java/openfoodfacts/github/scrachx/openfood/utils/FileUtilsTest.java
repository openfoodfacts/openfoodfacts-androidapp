package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class FileUtilsTest {
    private final String absoluteURL = "/path";
    private final String localURL = "file://path";

    @Test
    public void fileIsLocal_true() {
        assertTrue(FileUtils.isLocaleFile(localURL));
    }

    @Test
    public void fileIsLocal_false() {
        assertFalse(FileUtils.isLocaleFile(absoluteURL));
    }

    @Test
    public void isAbsolute_true(){
        assertTrue(FileUtils.isAbsolute(absoluteURL));
    }

    @Test
    public void isAbsolute_false(){
        assertFalse(FileUtils.isAbsolute(localURL));
    }

}