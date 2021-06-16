package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractMapFromJsonNode
import java.io.IOException

/**
 * Created by Lobster on 03.03.18.
 */
class AdditivesWrapperDeserializer : StdDeserializer<AdditivesWrapper>(AdditivesWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): AdditivesWrapper {
        val additives = mutableListOf<AdditiveResponse>()
        val mainNode = jp.codec.readTree<JsonNode>(jp)
        val mainNodeIterator = mainNode.fields()
        while (mainNodeIterator.hasNext()) {
            val subNode = mainNodeIterator.next()
            val namesNode = subNode.value[DeserializerHelper.NAMES_KEY]
            if (namesNode != null) {
                val names = extractMapFromJsonNode(namesNode)
                var overexposureRisk: String? = null
                var exposureMeanGreaterThanAdi: String? = null
                var exposureMeanGreaterThanNoael: String? = null
                var exposure95ThGreaterThanAdi: String? = null
                var exposure95ThGreaterThanNoael: String? = null
                if (subNode.value.has(EFSA_EVALUATION_OVEREXPOSURE_RISK_KEY)) {
                    // parse the overexposure risk the default value is "no"
                    overexposureRisk = subNode.value[EFSA_EVALUATION_OVEREXPOSURE_RISK_KEY][DeserializerHelper.EN_KEY].textValue().replaceFirst("^en:".toRegex(), "")

                    // update exposure evaluation map
                    if (subNode.value.has(EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_ADI)) {
                        exposureMeanGreaterThanAdi = subNode.value[EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_ADI][DeserializerHelper.EN_KEY].textValue()
                    }
                    if (subNode.value.has(EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_NOAEL)) {
                        exposureMeanGreaterThanNoael = subNode.value[EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_NOAEL][DeserializerHelper.EN_KEY].textValue()
                    }
                    if (subNode.value.has(EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_ADI)) {
                        exposure95ThGreaterThanAdi = subNode.value[EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_ADI][DeserializerHelper.EN_KEY].textValue()
                    }
                    if (subNode.value.has(EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_NOAEL)) {
                        exposure95ThGreaterThanNoael = subNode.value[EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_NOAEL][DeserializerHelper.EN_KEY].textValue()
                    }
                }
                val additiveResponse = if (subNode.value.has(DeserializerHelper.WIKIDATA_KEY)) {
                    AdditiveResponse(subNode.key, names, overexposureRisk, subNode.value[DeserializerHelper.WIKIDATA_KEY].toString())
                } else {
                    AdditiveResponse(subNode.key, names, overexposureRisk)
                }
                additiveResponse.setExposureEvalMap(exposure95ThGreaterThanAdi, exposure95ThGreaterThanNoael, exposureMeanGreaterThanAdi, exposureMeanGreaterThanNoael)
                additives.add(additiveResponse)
            }
        }
        return AdditivesWrapper(additives.toList())
    }

    companion object {
        private const val EFSA_EVALUATION_OVEREXPOSURE_RISK_KEY = "efsa_evaluation_overexposure_risk"
        private const val EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_ADI = "efsa_evaluation_exposure_95th_greater_than_adi"
        private const val EFSA_EVALUATION_EXPOSURE_95TH_GREATER_THAN_NOAEL = "efsa_evaluation_exposure_95th_greater_than_noael"
        private const val EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_ADI = "efsa_evaluation_exposure_mean_greater_than_adi"
        private const val EFSA_EVALUATION_EXPOSURE_MEAN_GREATER_THAN_NOAEL = "efsa_evaluation_exposure_mean_greater_than_noael"
    }
}