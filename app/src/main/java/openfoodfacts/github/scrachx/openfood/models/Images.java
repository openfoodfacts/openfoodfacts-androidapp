package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

/**
 * Created by prajwalm on 10/09/18.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Images implements Serializable {

    private List<Image> images;


}


