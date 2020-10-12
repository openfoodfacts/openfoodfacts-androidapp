package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.AdditivesWrapperDeserializer;

/**
 * Created by Lobster on 04.03.18.
 */

@JsonDeserialize(using = AdditivesWrapperDeserializer.class)
public class AdditivesWrapper {

    private List<AdditiveResponse> additives;

    public List<Additive> map() {
        List<Additive> entityLabels = new ArrayList<>();
        for (AdditiveResponse additive : additives) {
            entityLabels.add(additive.map());
        }

        return entityLabels;
    }

    public List<AdditiveResponse> getAdditives() {
        return additives;
    }

    public void setAdditives(List<AdditiveResponse> additives) {
        this.additives = additives;
    }
}
