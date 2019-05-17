package openfoodfacts.github.scrachx.openfood.jobs;

import java.io.File;

public interface PhotoReceiver {

     void onPhotoReturned(File newPhotoFile);
}
