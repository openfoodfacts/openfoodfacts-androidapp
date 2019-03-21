package openfoodfacts.github.scrachx.openfood.fragments;

import openfoodfacts.github.scrachx.openfood.utils.CustomValidatingEditTextView;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AddProductNutritionFactsData {
    static final List<String> PARAMS_OTHER_NUTRIENTS = Collections.unmodifiableList(Arrays.asList("nutriment_alpha-linolenic-acid",
        "nutriment_arachidic-acid",
        "nutriment_arachidonic-acid",
        "nutriment_behenic-acid",
        "nutriment_bicarbonate",
        "nutriment_biotin",
        "nutriment_butyric-acid",
        "nutriment_caffeine",
        "nutriment_calcium",
        "nutriment_capric-acid",
        "nutriment_caproic-acid",
        "nutriment_caprylic-acid",
        "nutriment_casein",
        "nutriment_cerotic-acid",
        "nutriment_chloride",
        "nutriment_cholesterol",
        "nutriment_chromium",
        "nutriment_copper",
        "nutriment_dihomo-gamma-linolenic-acid",
        "nutriment_docosahexaenoic-acid",
        "nutriment_eicosapentaenoic-acid",
        "nutriment_elaidic-acid",
        "nutriment_erucic-acid",
        "nutriment_fluoride",
        "nutriment_fructose",
        "nutriment_gamma-linolenic-acid",
        "nutriment_glucose",
        "nutriment_gondoic-acid",
        "nutriment_iodine",
        "nutriment_iron",
        "nutriment_lactose",
        "nutriment_lauric-acid",
        "nutriment_lignoceric-acid",
        "nutriment_linoleic-acid",
        "nutriment_magnesium",
        "nutriment_maltodextrins",
        "nutriment_maltose",
        "nutriment_manganese",
        "nutriment_mead-acid",
        "nutriment_melissic-acid",
        "nutriment_molybdenum",
        "nutriment_monounsaturated-fat",
        "nutriment_montanic-acid",
        "nutriment_myristic-acid",
        "nutriment_nervonic-acid",
        "nutriment_nucleotides",
        "nutriment_oleic-acid",
        "nutriment_omega-3-fat",
        "nutriment_omega-6-fat",
        "nutriment_omega-9-fat",
        "nutriment_palmitic-acid",
        "nutriment_pantothenic-acid",
        "nutriment_ph",
        "nutriment_phosphorus",
        "nutriment_polyols",
        "nutriment_polyunsaturated-fat",
        "nutriment_potassium",
        "nutriment_selenium",
        "nutriment_serum-proteins",
        "nutriment_silica",
        "nutriment_starch",
        "nutriment_stearic-acid",
        "nutriment_sucrose",
        "nutriment_taurine",
        "nutriment_trans-fat",
        "nutriment_vitamin-a",
        "nutriment_vitamin-b1",
        "nutriment_vitamin-b12",
        "nutriment_vitamin-b2",
        "nutriment_vitamin-pp",
        "nutriment_vitamin-b6",
        "nutriment_vitamin-b9",
        "nutriment_vitamin-c",
        "nutriment_vitamin-d",
        "nutriment_vitamin-e",
        "nutriment_vitamin-k",
        "nutriment_zinc"));
    static final String SUFFIX_UNIT = "_unit";
    static final String PREFIX_NUTRIMENT_LONG_NAME = "nutriment_";

    static String getCompleteEntryName(CustomValidatingEditTextView editText) {
        return PREFIX_NUTRIMENT_LONG_NAME + editText.getEntryName();
    }

    static String getShortName(String init) {
        return StringUtils.removeStart(init, PREFIX_NUTRIMENT_LONG_NAME);
    }
}
