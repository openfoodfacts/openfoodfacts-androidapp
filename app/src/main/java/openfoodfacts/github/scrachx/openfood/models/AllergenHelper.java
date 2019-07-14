package openfoodfacts.github.scrachx.openfood.models;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;

public class AllergenHelper {
    public static class Data {
        private final boolean incomplete;
        private final List<String> allergens;

        public Data(boolean incomplete, List<String> allergens) {
            this.incomplete = incomplete;
            this.allergens = allergens;
        }

        public boolean isIncomplete() {
            return incomplete;
        }

        public List<String> getAllergens() {
            return allergens;
        }

        public boolean isEmpty() {
            return !incomplete && CollectionUtils.isEmpty(allergens);
        }
    }

    private static Data createEmpty() {
        return new Data(false, Collections.emptyList());
    }

    public static Data computeUserAllergen(Product product, List<AllergenName> userAllergens) {
        if (userAllergens.isEmpty()) {
            return createEmpty();
        }

        if (!product.getStatesTags().contains("en:ingredients-completed")) {
            return new Data(true, Collections.emptyList());
        }

        Set<String> productAllergens = new HashSet<>(product.getAllergensHierarchy());
        productAllergens.addAll(product.getTracesTags());

        Set<String> allergenMatch = new TreeSet<>();
        for (AllergenName allergenName : userAllergens) {
            if (productAllergens.contains(allergenName.getAllergenTag())) {
                allergenMatch.add(allergenName.getName());
            }
        }
        return new Data(false, Collections.unmodifiableList(new ArrayList<>(allergenMatch)));
    }
}
