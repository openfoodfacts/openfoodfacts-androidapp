package openfoodfacts.github.scrachx.openfood.images;

import java.io.File;

/**
 * Defines all the functions of PhotoReceiver
 */
public interface PhotoReceiver {
    void onPhotoReturned(File newPhotoFile);
}
