package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.models.ProductImageField.OTHER;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class AddProductPhotosFragment extends BaseFragment implements PhotoReceiver {

    @BindView(R.id.btnAddOtherImage)
    ImageView imageOther;
    @BindView(R.id.imageProgress)
    ProgressBar imageProgress;
    @BindView(R.id.imageProgressText)
    TextView imageProgressText;
    private PhotoReceiverHandler photoReceiverHandler;
    @BindView(R.id.table_layout)
    TableLayout tableLayout;
    @BindView(R.id.btn_add)
    Button buttonAdd;

    private String code;
    private Activity activity;
    private File photoFile;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_product_photos, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoReceiverHandler=new PhotoReceiverHandler(this);
        Bundle b = getArguments();
        if (b != null) {
            Product product = (Product) b.getSerializable("product");
            OfflineSavedProduct offlineSavedProduct = (OfflineSavedProduct) b.getSerializable("edit_offline_product");
            boolean editionMode = b.getBoolean(AddProductActivity.KEY_IS_EDITION);
            if (product != null) {
                code = product.getCode();
            }
            if (editionMode && product != null) {
                buttonAdd.setText(R.string.save_edits);
            } else if (offlineSavedProduct != null) {
                code = offlineSavedProduct.getBarcode();
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_product_photos, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }


    @OnClick(R.id.btnAddOtherImage)
    void addOtherImage() {
        if (ContextCompat.checkSelfPermission(activity, CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    @OnClick(R.id.btn_add)
    void next() {
        Activity fragmentActivity = getActivity();
        if (fragmentActivity instanceof AddProductActivity) {
            ((AddProductActivity) fragmentActivity).proceed();
        }
    }

    @Override
    public void onPhotoReturned(File newPhotoFile) {
        photoFile = newPhotoFile;
        ProductImage image = new ProductImage(code, OTHER, photoFile);
        image.setFilePath(photoFile.toURI().getPath());
        if (activity instanceof AddProductActivity) {
            ((AddProductActivity) activity).addToPhotoMap(image, 4);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photoReceiverHandler.onActivityResult(this,requestCode,resultCode,data);
    }

    public void showImageProgress() {
        imageProgress.setVisibility(View.VISIBLE);
        imageProgressText.setVisibility(View.VISIBLE);
        imageProgressText.setText(R.string.toastSending);
        addImageRow();
    }

    public void hideImageProgress(boolean errorUploading, String message) {
        imageProgress.setVisibility(View.GONE);
        imageOther.setVisibility(View.VISIBLE);
        if (errorUploading) {
            imageProgressText.setVisibility(View.GONE);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        } else {
            imageProgressText.setText(R.string.image_uploaded_successfully);
        }
    }

    private void addImageRow() {
        TableRow image = new TableRow(activity);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dpsToPixels(100));
        lp.topMargin = dpsToPixels(10);
        ImageView imageView = new ImageView(activity);
        Picasso.with(activity)
                .load(photoFile)
                .resize(dpsToPixels(100), dpsToPixels(100))
                .centerInside()
                .into(imageView);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setLayoutParams(lp);
        image.addView(imageView);
        tableLayout.addView(image);
    }

}
