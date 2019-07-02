package openfoodfacts.github.scrachx.openfood.network.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.AdditiveResponse;
import openfoodfacts.github.scrachx.openfood.models.AdditivesWrapper;

/**
 * Created by Lobster on 03.03.18.
 */

public class AdditivesWrapperDeserializer extends StdDeserializer<AdditivesWrapper> {


    private static final String EFSA_EVALUATION_OVEREXPOSURE_RISK_KEY = "efsa_evaluation_overexposure_risk";
    private static final String EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_ADI = "efsa_evaluation_exposure_95th_greater_than_adi";
    private static final String EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_NOAEL = "efsa_evaluation_exposure_95th_greater_than_noael";
    private static final String EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_ADI = "efsa_evaluation_exposure_mean_greater_than_adi";
    private static final String EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_NOAEL = "efsa_evaluation_exposure_mean_greater_than_noael";

    public AdditivesWrapperDeserializer() {
        super(AdditivesWrapper.class);
    }

    @Override
    public AdditivesWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<AdditiveResponse> additives = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            final Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(DeserializerHelper.NAMES_KEY);

            if (namesNode != null) {
                Map<String, String> names = DeserializerHelper.extractNames(namesNode);

                String overexposureRisk = null;
                String exposureMeanGreaterThanAdi = null;
                String exposureMeanGreaterThanNoael = null;
                String exposure95ThGreaterThanAdi = null;
                String exposure95ThGreaterThanNoael = null;
                if( subNode.getValue().has( EFSA_EVALUATION_OVEREXPOSURE_RISK_KEY ) )
                {
                    // parse the overexposure risk the default value is "no"
                    overexposureRisk = subNode.getValue().get( EFSA_EVALUATION_OVEREXPOSURE_RISK_KEY ).
                            get( DeserializerHelper.EN_KEY ).textValue().replaceFirst("^en:", "");

                    // update exposure evaluation map
                    if( subNode.getValue().has( EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_ADI ) )
                    {
                        exposureMeanGreaterThanAdi = subNode.getValue().get( EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_ADI ).get( DeserializerHelper.EN_KEY ).textValue();
                    }

                    if( subNode.getValue().has( EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_NOAEL ) )
                    {
                        exposureMeanGreaterThanNoael = subNode.getValue().get( EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_NOAEL ).get( DeserializerHelper.EN_KEY ).textValue();
                    }

                    if( subNode.getValue().has( EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_ADI ) )
                    {
                        exposure95ThGreaterThanAdi = subNode.getValue().get( EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_ADI ).get( DeserializerHelper.EN_KEY ).textValue();
                    }

                    if( subNode.getValue().has( EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_NOAEL ) )
                    {
                        exposure95ThGreaterThanNoael = subNode.getValue().get( EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_NOAEL ).get( DeserializerHelper.EN_KEY ).textValue();
                    }
                }

                AdditiveResponse additiveResponse;
                if (subNode.getValue().has(DeserializerHelper.WIKIDATA_KEY)) {
                    additiveResponse = new AdditiveResponse(subNode.getKey(), names, overexposureRisk, subNode.getValue().get(DeserializerHelper.WIKIDATA_KEY).toString());
                } else {
                    additiveResponse = new AdditiveResponse(subNode.getKey(), names, overexposureRisk);
                }
                additiveResponse.setExposureEvalMap( exposure95ThGreaterThanAdi, exposure95ThGreaterThanNoael, exposureMeanGreaterThanAdi, exposureMeanGreaterThanNoael );
                additives.add(additiveResponse);
            }
        }


        AdditivesWrapper wrapper = new AdditivesWrapper();
        wrapper.setAdditives(additives);

        return wrapper;
    }
}
