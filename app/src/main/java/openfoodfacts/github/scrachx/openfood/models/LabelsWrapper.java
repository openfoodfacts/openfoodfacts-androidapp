package org.openfoodfacts.scanner.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import org.openfoodfacts.scanner.network.deserializers.LabelsWrapperDeserializer;

/**
 * Created by Lobster on 03.03.18.
 */

@JsonDeserialize(using = LabelsWrapperDeserializer.class)
public class LabelsWrapper {

    private List<LabelResponse> labels;

    public List<Label> map() {
        List<Label> entityLabels = new ArrayList<>();
        for (LabelResponse label : labels) {
            entityLabels.add(label.map());
        }

        return entityLabels;
    }

    public List<LabelResponse> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelResponse> labels) {
        this.labels = labels;
    }
}
