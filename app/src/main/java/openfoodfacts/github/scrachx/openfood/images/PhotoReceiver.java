package openfoodfacts.github.scrachx.openfood.images;

import java.io.File;

public interface PhotoReceiver {
     void onPhotoReturned(File newPhotoFile);
}
