package openfoodfacts.github.scrachx.openfood.views.product;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.*;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ProductAttributeDetailsFragment extends BottomSheetDialogFragment implements CustomTabActivityHelper.ConnectionCallback
{
	private static final String ARG_OBJECT = "result";
	private static final String ARG_ID = "code";
	private static final String ARG_SEARCH_TYPE = "search_type";
	private static final String ARG_TITLE = "title";

	private AppCompatImageView bottomSheetTitleIcon;

	private AppCompatImageView mpInfantsImage;
	private AppCompatImageView mpToddlersImage;
	private AppCompatImageView mpChildrenImage;
	private AppCompatImageView mpAdolescentsImage;
	private AppCompatImageView mpAdultsImage;
	private AppCompatImageView mpElderlyImage;
	private AppCompatImageView spInfantsImage;
	private AppCompatImageView spToddlersImage;
	private AppCompatImageView spChildrenImage;
	private AppCompatImageView spAdolescentsImage;
	private AppCompatImageView spAdultsImage;
	private AppCompatImageView spElderlyImage;

	private CustomTabsIntent customTabsIntent;

	public static ProductAttributeDetailsFragment newInstance(String jsonObjectStr, long id, String searchType, String title) {
		ProductAttributeDetailsFragment fragment = new ProductAttributeDetailsFragment();
		Bundle args = new Bundle();
		args.putString(ARG_OBJECT, jsonObjectStr);
		args.putLong(ARG_ID, id);
		args.putString(ARG_SEARCH_TYPE, searchType);
		args.putString(ARG_TITLE, title);

		fragment.setArguments(args);

		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView( LayoutInflater inflater,
							  @Nullable ViewGroup container,
							  @Nullable Bundle savedInstanceState )
	{
		CustomTabActivityHelper customTabActivityHelper = new CustomTabActivityHelper();
		customTabActivityHelper.setConnectionCallback( this );
		customTabsIntent = CustomTabsHelper.getCustomTabsIntent( getContext().getApplicationContext(), customTabActivityHelper.getSession() );

		View view = inflater.inflate( R.layout.fragment_product_attribute_details, container,
				false );

		TextView bottomSheetDescription = view.findViewById(R.id.description);
		TextView bottomSheetTitle = view.findViewById(R.id.titleBottomSheet);
		bottomSheetTitleIcon = view.findViewById( R.id.titleBottomSheetIcon );
		Button buttonToBrowseProducts = view.findViewById(R.id.buttonToBrowseProducts);
		Button wikipediaButton = view.findViewById(R.id.wikipediaButton);

		try
		{
			final String descriptionString;
			final String wikiLink;
			String str = getArguments().getString(ARG_OBJECT);
			if (str != null) {
				JSONObject result = new JSONObject(str);
				JSONObject description = result.getJSONObject("descriptions");
				JSONObject siteLinks = result.getJSONObject("sitelinks");

				descriptionString = getDescription(description);
				wikiLink = getWikiLink(siteLinks);
			} else {
				descriptionString = null;
				wikiLink = null;
			}

			String title = getArguments().getString(ARG_TITLE);
			bottomSheetTitle.setText(title);
			String searchType = getArguments().getString(ARG_SEARCH_TYPE);
			if (descriptionString != null) {
				bottomSheetDescription.setText(descriptionString);
				bottomSheetDescription.setVisibility(View.VISIBLE);
			} else {
				bottomSheetDescription.setVisibility(View.GONE);
			}

			buttonToBrowseProducts.setOnClickListener( v -> ProductBrowsingListActivity.startActivity(getContext(), title, searchType));
			if (wikiLink != null) {
				wikipediaButton.setOnClickListener(v -> openInCustomTab(wikiLink));
				wikipediaButton.setVisibility(View.VISIBLE);
			} else {
				wikipediaButton.setVisibility(View.GONE);
			}

			long id = getArguments().getLong( ARG_ID );
			switch( searchType )
			{
				case SearchType.ADDITIVE:
				{
					AdditiveNameDao dao = Utils.getAppDaoSession( getActivity() ).getAdditiveNameDao();
					AdditiveName additiveName = dao.queryBuilder()
							.where(
									AdditiveNameDao.Properties.Id.eq( id )
							).unique();
					updateContent( view, additiveName );
				}
				break;
				case SearchType.CATEGORY:
				{
					CategoryNameDao dao = Utils.getAppDaoSession( getActivity() ).getCategoryNameDao();
					CategoryName categoryName = dao.queryBuilder()
							.where(
									CategoryNameDao.Properties.Id.eq( id )
							).unique();
					updateContent( view, categoryName );
				}
				break;
				case SearchType.LABEL:
				{
					LabelNameDao dao = Utils.getAppDaoSession( getActivity() ).getLabelNameDao();
					LabelName labelName = dao.queryBuilder()
							.where(
									LabelNameDao.Properties.Id.eq( id )
							).unique();
					updateContent( view, labelName );
				}
				break;
			}
		}
		catch( JSONException e )
		{
			e.printStackTrace();
		}

		return view;
	}

	@Override
	public void onViewCreated( @NonNull View view, @Nullable Bundle savedInstanceState )
	{
		super.onViewCreated( view, savedInstanceState );
	}

	private void updateContent( View view, AdditiveName additive )
	{
		View exposureEvalTable = view.findViewById(R.id.exposureEvalTable);
		mpInfantsImage = view.findViewById( R.id.mpInfants );
		mpToddlersImage = view.findViewById( R.id.mpToddlers );
		mpChildrenImage = view.findViewById( R.id.mpChildren );
		mpAdolescentsImage = view.findViewById( R.id.mpAdolescents );
		mpAdultsImage = view.findViewById( R.id.mpAdults );
		mpElderlyImage = view.findViewById( R.id.mpElderly );
		spInfantsImage = view.findViewById( R.id.spInfants );
		spToddlersImage = view.findViewById( R.id.spToddlers );
		spChildrenImage = view.findViewById( R.id.spChildren );
		spAdolescentsImage = view.findViewById( R.id.spAdolescents );
		spAdultsImage = view.findViewById( R.id.spAdults );
		spElderlyImage = view.findViewById( R.id.spElderly );

		TextView efsaWarning = view.findViewById( R.id.efsaWarning );

		String overexposureRisk = additive.getOverexposureRisk();
		if( additive.hasOverexposureData() )
		{
			boolean isHighRisk = "high".equalsIgnoreCase( overexposureRisk );
			if( isHighRisk )
			{
				bottomSheetTitleIcon.setImageResource( R.drawable.ic_additive_high_risk );
				efsaWarning.setText( getString( R.string.efsa_warning_high_risk, additive.getName() ) );
			}
			else
			{
				bottomSheetTitleIcon.setImageResource( R.drawable.ic_additive_moderate_risk );
				efsaWarning.setText( getString( R.string.efsa_warning_high_risk, additive.getName() ) );
			}
			bottomSheetTitleIcon.setVisibility( View.VISIBLE );

			// noel will override adi evaluation if present
			updateAdditiveExposureTable( 0, additive.getExposureMeanGreaterThanAdi(), R.drawable.yellow_circle );
			updateAdditiveExposureTable( 0, additive.getExposureMeanGreaterThanNoael(), R.drawable.red_circle );
			updateAdditiveExposureTable( 1, additive.getExposure95ThGreaterThanAdi(), R.drawable.yellow_circle );
			updateAdditiveExposureTable( 1, additive.getExposure95ThGreaterThanNoael(), R.drawable.red_circle );

			exposureEvalTable.setVisibility( View.VISIBLE );
		}
	}

	private void updateAdditiveExposureTable( int row, String exposure, int drawableResId )
	{
		if( row == 0 )
		{
			if( exposure != null )
			{
				if( exposure.contains( "infants" ) )
				{
					mpInfantsImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "toddlers" ) )
				{
					mpToddlersImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "children" ) )
				{
					mpChildrenImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "adolescents" ) )
				{
					mpAdolescentsImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "adults" ) )
				{
					mpAdultsImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "elderly" ) )
				{
					mpElderlyImage.setImageResource( drawableResId );
				}
			}
		}
		else if( row == 1 )
		{
			if( exposure != null )
			{
				if( exposure.contains( "infants" ) )
				{
					spInfantsImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "toddlers" ) )
				{
					spToddlersImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "children" ) )
				{
					spChildrenImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "adolescents" ) )
				{
					spAdolescentsImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "adults" ) )
				{
					spAdultsImage.setImageResource( drawableResId );
				}

				if( exposure.contains( "elderly" ) )
				{
					spElderlyImage.setImageResource( drawableResId );
				}
			}
		}
	}

	private void updateContent( View view, CategoryName category )
	{
	}

	private void updateContent( View view, LabelName label )
	{
	}

	private String getDescription( JSONObject description )
	{
		String descriptionString = "";
		String languageCode = Locale.getDefault().getLanguage();
		if( description.has( languageCode ) )
		{
			try
			{
				description = description.getJSONObject( languageCode );
				descriptionString = description.getString( "value" );
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		}
		else if( description.has( "en" ) )
		{
			try
			{
				description = description.getJSONObject( "en" );
				descriptionString = description.getString( "value" );
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			Log.i( "ProductActivity", "Result for description is not found in native or english language." );
		}
		return descriptionString;
	}

	private String getWikiLink( JSONObject sitelinks )
	{
		String link = "";
		String languageCode = Locale.getDefault().getLanguage();
		languageCode = languageCode + "wiki";
		if( sitelinks.has( languageCode ) )
		{
			try
			{
				sitelinks = sitelinks.getJSONObject( languageCode );
				link = sitelinks.getString( "url" );
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		}
		else if( sitelinks.has( "enwiki" ) )
		{
			try
			{
				sitelinks = sitelinks.getJSONObject( "enwiki" );
				link = sitelinks.getString( "url" );
			}
			catch( JSONException e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			Log.i( "ProductActivity", "Result for wikilink is not found in native or english language." );
		}
		return link;
	}

	private void openInCustomTab( String url )
	{
		// Url might be empty string if there is no wiki link in english or the user's language
		if( !url.equals( "" ) )
		{
			Uri wikipediaUri = Uri.parse( url );
			CustomTabActivityHelper.openCustomTab( getActivity(), customTabsIntent, wikipediaUri, new WebViewFallback() );
		}
		else
		{
			Toast.makeText( getContext(), R.string.wikidata_unavailable, Toast.LENGTH_SHORT ).show();
		}
	}

	@Override
	public void onCustomTabsConnected()
	{

	}

	@Override
	public void onCustomTabsDisconnected()
	{

	}
}
