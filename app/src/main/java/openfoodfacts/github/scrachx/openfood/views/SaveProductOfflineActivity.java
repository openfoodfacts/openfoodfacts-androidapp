package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;

public class SaveProductOfflineActivity extends Activity{

    ImageView imgSave;
    EditText name, energy, store, weight;
    Button takePic, save;
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;
    File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_product_offline);

        final SharedPreferences settings = getSharedPreferences("img", 0);
        settings.getString("imgUrl", "");

        Intent intent = getIntent();
        final String barcode = intent.getStringExtra("barcode");

        final String[] unit = new String[1];

        imgSave = (ImageView) findViewById(R.id.imageSave);
        name = (EditText) findViewById(R.id.editTextName);
        store = (EditText) findViewById(R.id.editTextStores);
        weight = (EditText) findViewById(R.id.editTextWeight);
        takePic = (Button) findViewById(R.id.buttonTakePicture);
        save = (Button) findViewById(R.id.buttonSaveProduct);

        name.setSelected(false);
        store.setSelected(false);
        weight.setSelected(false);

        Spinner spinner = (Spinner) findViewById(R.id.spinnerUnitWeight);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.units_array, R.layout.custom_spinner_item);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                unit[0] = parent.getItemAtPosition(pos).toString();
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        List<SendProduct> sp = SendProduct.find(SendProduct.class,"barcode = ?", barcode);
        if(sp.size() > 0){
            SendProduct product = sp.get(0);
            Bitmap bt = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(product.getImgupload_front()), 400, 400, true);
            imgSave.setImageBitmap(bt);
            name.setText(product.getName());
            store.setText(product.getStores());
            weight.setText(product.getWeight());
            ArrayAdapter unitAdapter = (ArrayAdapter) spinner.getAdapter(); //cast to an ArrayAdapter
            int spinnerPosition = unitAdapter.getPosition(product.getWeight_unit());
            spinner.setSelection(spinnerPosition);
        }

        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(settings);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!settings.getString("imgUrl", "").isEmpty() && !name.getText().toString().isEmpty()){
                    List<SendProduct> sp = SendProduct.find(SendProduct.class,"barcode = ?", barcode);
                    if(sp.size() > 0){
                        SendProduct product = sp.get(0);
                        product.setName(name.getText().toString());
                        product.setImgupload_front(settings.getString("imgUrl", ""));
                        product.setStores(store.getText().toString());
                        product.setWeight(weight.getText().toString());
                        product.setWeight_unit(unit[0]);
                        product.save();
                    }else{
                        SendProduct product = new SendProduct(barcode, name.getText().toString(), "",
                                "", weight.getText().toString(), unit[0], settings.getString("imgUrl", ""), store.getText().toString());
                        product.save();
                    }
                    Toast.makeText(getApplicationContext(), R.string.txtDialogsContentInfoSave, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    SaveProductOfflineActivity.this.finish();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.txtPictureNeeded, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        final SharedPreferences settings = getSharedPreferences("img", 0);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Bitmap bt = BitmapFactory.decodeFile(settings.getString("imgUrl", ""));
            imgSave.setImageBitmap(bt);
        }

    }

    private void dispatchTakePictureIntent(SharedPreferences shpref) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                SharedPreferences.Editor editor = shpref.edit();
                photoFile = createImageFile();
                editor.putString("imgUrl", photoFile.getAbsolutePath());
                editor.commit();
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.println(ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
