package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 04.03.18.
 */

public class AdditiveResponse {

    private String tag;

    private Map<String, String> names;

    private String wikiDataCode;
    private Boolean isWikiDataIdPresent;
    private String overexposureRisk;
    private String exposureMeanGreaterThanAdi;
    private String exposureMeanGreaterThanNoael;
    private String exposure95ThGreaterThanAdi;
    private String exposure95ThGreaterThanNoael;

    public AdditiveResponse( String tag, Map<String, String> names, String overexposureRisk, String wikiDataCode )
    {
        this.tag = tag;
        this.names = names;
        this.wikiDataCode = wikiDataCode;
        this.overexposureRisk = overexposureRisk;
        this.isWikiDataIdPresent = true;
    }

    public AdditiveResponse( String tag, Map<String, String> names, String overexposureRisk )
    {
        this.tag = tag;
        this.names = names;
        this.overexposureRisk = overexposureRisk;
        this.isWikiDataIdPresent = false;
    }

    public void setExposureEvalMap( String exposure95ThGreaterThanAdi, String exposure95ThGreaterThanNoael, String exposureMeanGreaterThanAdi, String exposureMeanGreaterThanNoael )
    {
        this.exposure95ThGreaterThanAdi = exposure95ThGreaterThanAdi;
        this.exposure95ThGreaterThanNoael = exposure95ThGreaterThanNoael;
        this.exposureMeanGreaterThanAdi = exposureMeanGreaterThanAdi;
        this.exposureMeanGreaterThanNoael = exposureMeanGreaterThanNoael;
    }

    public Additive map() {
        Additive additive;

        if (isWikiDataIdPresent) {
            additive = new Additive(tag, new ArrayList<>(), overexposureRisk, wikiDataCode);
            for (Map.Entry<String, String> name : names.entrySet()) {
                AdditiveName additiveName = new AdditiveName(additive.getTag(), name.getKey(), name.getValue(), overexposureRisk, wikiDataCode);
                additiveName.setExposureEvalMap( exposure95ThGreaterThanAdi, exposure95ThGreaterThanNoael, exposureMeanGreaterThanAdi, exposureMeanGreaterThanNoael );
                additive.getNames().add(additiveName);
            }
        } else {
            additive = new Additive(tag, new ArrayList<>(), overexposureRisk );
            for (Map.Entry<String, String> name : names.entrySet()) {
                AdditiveName additiveName = new AdditiveName( additive.getTag(), name.getKey(), name.getValue(), overexposureRisk );
                additiveName.setExposureEvalMap( exposure95ThGreaterThanAdi, exposure95ThGreaterThanNoael, exposureMeanGreaterThanAdi, exposureMeanGreaterThanNoael );
                additive.getNames().add( additiveName );
            }
        }

        additive.setExposureEvalMap( exposure95ThGreaterThanAdi, exposure95ThGreaterThanNoael, exposureMeanGreaterThanAdi, exposureMeanGreaterThanNoael );

        return additive;
    }

}
