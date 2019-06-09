package openfoodfacts.github.scrachx.openfood.jobs;

import java.io.File;

/**
 * Defines all the functions of PhotoReceiver
 */
public interface PhotoReceiver {

     void onPhotoReturned(File newPhotoFile);
}
