package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;

/**
 * Created by scotscriven on 09/05/15.
 */
public class SaveProductOffline extends Activity{

    ImageView imgSave;
    EditText name, energy, store, weight;
    Button takePic, save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_product_offline);

        imgSave = (ImageView) findViewById(R.id.imageSave);
        name = (EditText) findViewById(R.id.editTextName);
        energy = (EditText) findViewById(R.id.editTextEnergy);
        store = (EditText) findViewById(R.id.editTextStores);
        weight = (EditText) findViewById(R.id.editTextWeight);
        takePic = (Button) findViewById(R.id.buttonTakePicture);
        save = (Button) findViewById(R.id.buttonSave);

        Intent intent = this.getIntent();
        final String barcode = intent.getStringExtra("barcode");

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendProduct product = new SendProduct(barcode, name.getText().toString(), energy.getText().toString(),
                        "kJ", weight.getText().toString(), "urlImghere", store.getText().toString());
                product.save();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
