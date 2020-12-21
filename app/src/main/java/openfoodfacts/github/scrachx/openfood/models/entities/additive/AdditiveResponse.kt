package openfoodfacts.github.scrachx.openfood.models.entities.additive

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse

/**
 * Created by Lobster on 04.03.18.
 */
class AdditiveResponse(
        private val tag: String,
        private val names: Map<String, String>,
        private val overexposureRisk: String?,
        private var wikiDataCode: String? = null
) : EntityResponse<Additive> {
    private var exposureMeanGreaterThanAdi: String? = null
    private var exposureMeanGreaterThanNoael: String? = null
    private var exposure95ThGreaterThanAdi: String? = null
    private var exposure95ThGreaterThanNoael: String? = null


    fun setExposureEvalMap(
            exposure95ThGreaterThanAdi: String?,
            exposure95ThGreaterThanNoael: String?,
            exposureMeanGreaterThanAdi: String?,
            exposureMeanGreaterThanNoael: String?
    ) {
        this.exposure95ThGreaterThanAdi = exposure95ThGreaterThanAdi
        this.exposure95ThGreaterThanNoael = exposure95ThGreaterThanNoael
        this.exposureMeanGreaterThanAdi = exposureMeanGreaterThanAdi
        this.exposureMeanGreaterThanNoael = exposureMeanGreaterThanNoael
    }

    override fun map(): Additive {
        val additive: Additive
        if (wikiDataCode != null) {
            additive = Additive(tag, arrayListOf(), overexposureRisk, wikiDataCode)
            names.forEach { (key, value) ->
                val additiveName = AdditiveName(additive.tag, key, value, overexposureRisk, wikiDataCode)
                additiveName.setExposureEvalMap(exposure95ThGreaterThanAdi, exposure95ThGreaterThanNoael, exposureMeanGreaterThanAdi, exposureMeanGreaterThanNoael)
                additive.names.add(additiveName)
            }
        } else {
            additive = Additive(tag, arrayListOf(), overexposureRisk)
            names.forEach { (key, value) ->
                val additiveName = AdditiveName(additive.tag, key, value, overexposureRisk)
                additiveName.setExposureEvalMap(exposure95ThGreaterThanAdi, exposure95ThGreaterThanNoael, exposureMeanGreaterThanAdi, exposureMeanGreaterThanNoael)
                additive.names.add(additiveName)
            }
        }
        additive.setExposureEvalMap(exposure95ThGreaterThanAdi, exposure95ThGreaterThanNoael, exposureMeanGreaterThanAdi, exposureMeanGreaterThanNoael)
        return additive
    }
}