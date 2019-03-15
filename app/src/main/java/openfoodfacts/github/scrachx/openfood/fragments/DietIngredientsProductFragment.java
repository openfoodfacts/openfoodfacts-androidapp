package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductIngredient;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.repositories.DietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IDietRepository;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.DietHAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.DietIngredientsProductAdapter;

public class DietIngredientsProductFragment extends BaseFragment {

    RecyclerView dietRV;
    RecyclerView ingredientsRV;
    DietIngredientsProductAdapter ingredientsRVAdapter;
    int lastDietDisplayed = 0;
    boolean smoothScroll = true;

    private Product product;
    private State mState;
    private Diet mDiet;
    private List<SpannableStringBuilder> mIngredients;
    private IDietRepository dietRepository;
    // Fetching of the (theoretical) language of input:
    //To be replaced with the product.getLang() !!!
    private String languageCode = Locale.getDefault().getLanguage();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dietRepository = DietRepository.getInstance();

        Intent intent = getActivity().getIntent();
        mState = (State) intent.getExtras().getSerializable("state");
        product = mState.getProduct();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //No diet, no fragment !!!
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        DietDao dietDao = daoSession.getDietDao();
        if (dietDao.loadAll().size() == 0) {
            return null;
        }
        return inflater.inflate(R.layout.fragment_diet_ingredients_product, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = getActivity().getIntent();
        mState = (State) intent.getExtras().getSerializable("state");
        refreshView(mState);

        dietRV = (RecyclerView) view.findViewById(R.id.diet_h_recycler);
        ingredientsRV = (RecyclerView) view.findViewById(R.id.ingredients_recyclerView);
        //définit l'agencement des cellules, ici de façon verticale, comme une ListView
        dietRV.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        ingredientsRV.setLayoutManager(new LinearLayoutManager(this.getContext()));
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        DietDao dietDao = daoSession.getDietDao();
        List<Diet> dietList = dietDao.loadAll();
        Collections.sort(dietList, new Comparator<Diet>() {
            @Override
            public int compare(Diet d1, Diet d2) {
                if (d1.getEnabled()) {
                    if (d2.getEnabled()) {
                        return d1.getTag().substring(3).compareToIgnoreCase(d2.getTag().substring(3));
                    } else {
                        return -1;
                    }
                } else if (d2.getEnabled()) {
                    return 1;
                } else {
                    return d1.getTag().substring(3).compareToIgnoreCase(d2.getTag().substring(3));
                }
            }
        });
        mDiet = dietList.get(0);
        dietRV.setAdapter(new DietHAdapter(dietList, new ClickListener() {
            @Override
            public void onPositionClicked(int position, View v) {
                int changeToPosition = position<dietRV.getAdapter().getItemCount()-1 ? position+1 : 0;
                dietRV.smoothScrollToPosition(changeToPosition);
                changeMDiet(dietList.get(changeToPosition));
            }

            @Override
            public void onLongClicked(int position, View v) {
                dietRV.scrollToPosition(position-1);
                changeMDiet(dietList.get(position-1));
            }
        }));
        dietRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //SmoothScroll to the first or last visible Diet Item
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 0) {
                    if (smoothScroll) {
                        smoothScroll = false;
                        LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (lastDietDisplayed > llm.findFirstVisibleItemPosition()) {
                            lastDietDisplayed = llm.findFirstVisibleItemPosition();
                        } else {
                            lastDietDisplayed = llm.findLastVisibleItemPosition();
                        }
                        recyclerView.smoothScrollToPosition(lastDietDisplayed);
                        changeMDiet(dietList.get(lastDietDisplayed));
                        //TODO Change the ingredients state to be on phase to the Diet.
                    } else {
                        smoothScroll = true;
                    }
                }
            }
        });

        if (mState != null && product.getIngredients() != null) {
            languageCode = product.getLang();
            fillIngredients(coloredIngredientsFromProduct());
        }
    }

    private void changeMDiet(Diet diet) {
        mDiet = diet;
        mIngredients.clear();
        mIngredients.addAll(coloredIngredientsFromProduct());
        ingredientsRVAdapter.notifyDataSetChanged();
    }

    private List<SpannableStringBuilder> coloredIngredientsFromProduct() {
        return dietRepository.getColoredSSBFromProductAndDiet(product, mDiet.getTag());
    }

    public void fillIngredients(List<SpannableStringBuilder> ingredients) {
        mIngredients = ingredients;
        ingredientsRVAdapter = new DietIngredientsProductAdapter(mIngredients, new ClickListener() {
            @Override
            public void onPositionClicked(int position, View v) {
                //addDietTagIngredients because languageCode of the product is not necessary languageCode of the Diet !
                String ingredientTag = "";
                List<ProductIngredient> productIngredients = product.getIngredients();
                for (int i = 0; i < productIngredients.size(); i++) {
                    ProductIngredient productIngredient =  productIngredients.get(i);
                    if (productIngredient.getText().replace("_","").equals(mIngredients.get(position).toString())) {
                        ingredientTag = productIngredient.getId();
                        //Sometimes ID passed from a product doesn't exists in the taxonomy from ingredient.json.
                        if (dietRepository.getIngredientByTag(productIngredient.getId()).getTag() == null) {
                            //That's it, create a new ingredient
                            dietRepository.addIngredient(productIngredient.getId(), productIngredient.getText(), productIngredient.getId().split(":")[0]);
                        }
                        break;
                    }
                }
                if (ingredientTag.equals("")) {
                    dietRepository.addDietIngredients(mDiet.getTag(), mIngredients.get(position).toString(), languageCode, stateFromView(v));
                } else {
                    dietRepository.addDietIngredientsByTags(mDiet.getTag(),ingredientTag, stateFromView(v));
                }
                changeMDiet(mDiet);
            }
            @Override
            public void onLongClicked(int position, View v) {
                String ingredientTag = "";
                List<ProductIngredient> productIngredients = product.getIngredients();
                for (int i = 0; i < productIngredients.size(); i++) {
                    ProductIngredient productIngredient =  productIngredients.get(i);
                    if (productIngredient.getText().replace("_","").equals(mIngredients.get(position).toString())) {
                        ingredientTag = productIngredient.getId();
                        //Sometimes ID passed from a product doesn't exists in the taxonomy from ingredient.json.
                        if (dietRepository.getIngredientByTag(productIngredient.getId()).getTag() != null) {
                            //That's it, create a new ingredient
                            dietRepository.addIngredient(productIngredient.getId(), productIngredient.getText(), productIngredient.getId().split(":")[0]);
                        }
                        break;
                    }
                }
                List<Diet> dietsEnabled = dietRepository.getEnabledDiets();
                for (int i = 0; i < dietsEnabled.size(); i++) {
                    Diet diet =  dietsEnabled.get(i);
                    if (ingredientTag.equals("")) {
                        dietRepository.addDietIngredients(diet.getTag(), mIngredients.get(position).toString(), languageCode, stateFromView(v));
                    } else {
                        dietRepository.addDietIngredientsByTags(diet.getTag(),ingredientTag, stateFromView(v));
                    }
                }
                changeMDiet(mDiet);
            }
        });
        ingredientsRV.setAdapter(ingredientsRVAdapter);
    }

    public Diet getmDiet() {
        return mDiet;
    }

    private int stateFromView(View v) {
        int state = 2;
        String vName = v.toString().substring(v.toString().indexOf("state")+5);
        if (vName.equals("GreenImageButton}")) {
            state = 1;
        } else if (vName.equals("OrangeImageButton}")) {
            state=0;
        } else if (vName.equals("RedImageButton}")) {
            state=-1;
        }
        return state;
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        mState = state;
    }

    public interface ClickListener {
        void onPositionClicked(int position, View v);
        void onLongClicked(int position, View v);
    }
}
