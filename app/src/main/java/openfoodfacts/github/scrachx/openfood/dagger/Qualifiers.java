package openfoodfacts.github.scrachx.openfood.dagger;

import javax.inject.Qualifier;

public class Qualifiers {

    @Qualifier
    public @interface ForApplication {
    }

    @Qualifier
    public @interface ForActivity {
    }
}
