package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietIngredients;
import openfoodfacts.github.scrachx.openfood.models.DietName;
import openfoodfacts.github.scrachx.openfood.repositories.DietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IDietRepository;

public class EditDietFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DIET_NAME = "dietName";
    //Récupération des éléments du fragment de saisie d'une Diet
    @BindView(R.id.diet_name)
    EditText dietName;
    @BindView(R.id.diet_description)
    EditText dietDescription;
    @BindView(R.id.diet_enabled)
    Switch dietEnabled;
    @BindView(R.id.ingredients_authorised)
    EditText ingredientsAuthorised;
    @BindView(R.id.ingredients_soso)
    EditText ingredientsSoSo;
    @BindView(R.id.ingredients_unauthorised)
    EditText ingredientsUnauthorised;
    @BindView(R.id.save_edits)
    Button saveEdits;
    // Fetching of the (theoretical) language of input:
    private String languageCode = Locale.getDefault().getLanguage();
    private IDietRepository dietRepository;


    // TODO: Rename and change types of parameters
    private String mDietName;

    //private OnFragmentInteractionListener mListener;

    public EditDietFragment() {
        //Log.i("INFO", "Début de EditDietFragment() de FragmentEditDiet");
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param dietName Name off the diet to edit, null if new diet to be created.
     * @return A new instance of fragment EditDietFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditDietFragment newInstance(String dietName) {
        //Log.i("INFO", "Début de EditDietFragment(" + dietName + ") de FragmentEditDiet");
        EditDietFragment fragment = new EditDietFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIET_NAME, dietName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i("INFO", "Début de OnCreate de FragmentEditDiet");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDietName = getArguments().getString(ARG_DIET_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.i("INFO", "Début de OnCreateView de FragmentEditDiet");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_diet, container, false);
        ButterKnife.bind(this, view);
        if (mDietName != null) {
            IDietRepository dietRepository = DietRepository.getInstance();
            Diet diet = dietRepository.getDietByNameAndLanguageCode(mDietName, languageCode);
            dietName.setText(mDietName);
            DietName dietName = dietRepository.getDietNameByDietTagAndLanguageCode(diet.getTag(), languageCode);
            dietDescription.setText(dietName.getDescription());
            dietEnabled.setChecked(diet.getEnabled());
            ingredientsAuthorised.setText(dietRepository.getSortedIngredientNameStringByDietTagStateAndLanguageCode(diet.getTag(), 1, languageCode));
            ingredientsSoSo.setText(dietRepository.getSortedIngredientNameStringByDietTagStateAndLanguageCode(diet.getTag(), 0, languageCode));
            ingredientsUnauthorised.setText(dietRepository.getSortedIngredientNameStringByDietTagStateAndLanguageCode(diet.getTag(), -1, languageCode));
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //Log.i("INFO", "Début de OnActivityCreated de FragmentEditDiet");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //Log.i("INFO", "Début de OnViewCreated de FragmentEditDiet");
        super.onViewCreated(view, savedInstanceState);
        //Log.i("INFO", "Fin de OnViewCreated de FragmentEditDiet");
    }

    @OnClick(R.id.save_edits)
    void saveEdits() {
        //Log.i("INFO", "Début de saveEdits de FragmentEditDiet");
        //Log.i("INFO", "dietName : " + dietName.getText() + " dietDescription : " + dietDescription.getText() + " dietEnabled : " + dietEnabled.isChecked() + " ingredientsAuthorised : " + ingredientsAuthorised.getText() + " ingredientsSoSo : " + ingredientsSoSo.getText() + " ingredientsUnauthorised : " + ingredientsUnauthorised.getText());
        dietRepository = DietRepository.getInstance();
        //Add or replace a diet row with the form's informations.
        dietRepository.addDiet(dietName.getText().toString(), dietDescription.getText().toString(), dietEnabled.isChecked(), languageCode);
        //Get the diet
        Diet diet = dietRepository.getDietByNameAndLanguageCode(dietName.getText().toString(), languageCode);
        //Set the state at 2 (no impact) for all ingredients associated with the diet. Sort of reset before the nexts steps
        List<DietIngredients> dietIngredientsList = dietRepository.getDietIngredientsListByDietTag(diet.getTag());
        for (int i = 0; i < dietIngredientsList.size(); i++) {
            DietIngredients dietIngredients =  dietIngredientsList.get(i);
            dietIngredients.setState(2);
            dietRepository.saveDietIngredients(dietIngredients);
        }
        //Set the goods state for each ingredients in the 3 lists
        for (String ingredient : ingredientsAuthorised.getText().toString().split(",")) {
            dietRepository.addIngredient(ingredient, languageCode);
            dietRepository.addDietIngredients(diet.getTag(), ingredient, languageCode,1);
        }
        for (String ingredient : ingredientsSoSo.getText().toString().split(",")) {
            dietRepository.addIngredient(ingredient, languageCode);
            dietRepository.addDietIngredients(diet.getTag(), ingredient, languageCode,0);
        }
        for (String ingredient : ingredientsUnauthorised.getText().toString().split(",")) {
            dietRepository.addIngredient(ingredient, languageCode);
            dietRepository.addDietIngredients(diet.getTag(), ingredient, languageCode,-1);
        }
        //Back to the DietsFragment.
        Fragment fragment = new DietsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment );
        transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
        transaction.commit();
    }
    //TODO Add a delete button witch suppress rows of DietIngredients, DietName and Diet
    //TODO Change name only change row and don't create a new one.
    //TODO Internationalisation made easy : en:Vegetarian, fr:Végétarien
    //TODO Export Diet and associated DietName, DietIngredients, Ingredients and IngredientName for sharing with friends and family (very usefull when you go and eat at a friend's house).
    //Others todos without direct link with this Class.
    //TODO Internationalised ingredients.
    //TODO Add a Red or Orange traffic Light on the first resume that appear when you scan a product if at list one ingredient is forbidden or at list one ingredient is so-so
}
