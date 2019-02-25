package openfoodfacts.github.scrachx.openfood.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import openfoodfacts.github.scrachx.openfood.R;


public class FeedBackDialog {

    private Context mContext;


    private Drawable mIcon;

    @ColorRes
    private int mIconColor;

    private String mTitle;

    @ColorRes
    private int mBackgroundColor;

    private String mDescription;

    private String mReviewQuestion;


    private ImageView titleImageView;
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView reviewQuestionTextView;

    private LinearLayout positiveFeedbackLayout;
    private LinearLayout negativeFeedbackLayout;
    private LinearLayout ambiguityFeedbackLayout;
    private LinearLayout feedbackBodyLayout;

    private TextView positiveFeedbackTextView;
    private TextView negativeFeedbackTextView;
    private TextView ambiguityFeedbackTextView;

    private ImageView positiveFeedbackIconView;
    private ImageView negativeFeedbackIconView;
    private ImageView ambiguityFeedbackIconView;


    private String mPositiveFeedbackText;

    private Drawable mPositiveFeedbackIcon;

    private String mNegativeFeedbackText;

    private Drawable mNegativeFeedbackIcon;

    private String mAmbiguityFeedbackText;

    private Drawable mAmbiguityFeedbackIcon;

    private Dialog mDialog;

    private FeedBackActionsListeners mReviewActionsListener;

    public FeedBackDialog(Context mContext)
    {
        this.mContext = mContext;

        mDialog = new Dialog(mContext,R.style.FeedbackDialog_Theme_Dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.review_dialog_base);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            int width = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.90);
            int height = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.50);

            if (mDialog.getWindow() != null) {
                mDialog.getWindow().setLayout(width, height);
            }
        }
    }

    private void initiateAllViews()
    {
        titleImageView          = (ImageView) mDialog.findViewById(R.id.review_icon);
        titleTextView           = (TextView) mDialog.findViewById(R.id.review_title);
        descriptionTextView     = (TextView) mDialog.findViewById(R.id.review_description);
        reviewQuestionTextView  = (TextView) mDialog.findViewById(R.id.review_questions);

        feedbackBodyLayout      = (LinearLayout) mDialog.findViewById(R.id.feedback_body_layout);

        positiveFeedbackLayout = (LinearLayout) mDialog.findViewById(R.id.postive_feedback_layout);
        negativeFeedbackLayout = (LinearLayout) mDialog.findViewById(R.id.negative_feedback_layout);
        ambiguityFeedbackLayout = (LinearLayout) mDialog.findViewById(R.id.ambiguity_feedback_layout);


        positiveFeedbackTextView = (TextView) mDialog.findViewById(R.id.positive_feedback_text);
        negativeFeedbackTextView = (TextView) mDialog.findViewById(R.id.negative_feedback_text);
        ambiguityFeedbackTextView = (TextView) mDialog.findViewById(R.id.ambiguity_feedback_text);

        positiveFeedbackIconView = (ImageView) mDialog.findViewById(R.id.postive_feedback_icon);
        negativeFeedbackIconView = (ImageView) mDialog.findViewById(R.id.negative_feedback_icon);
        ambiguityFeedbackIconView = (ImageView) mDialog.findViewById(R.id.ambiguity_feedback_icon);
    }

    private void initiateListeners()
    {

        positiveFeedbackLayout.setOnClickListener(this::onPositiveFeedbackClicked);

        negativeFeedbackLayout.setOnClickListener(this::onNegativeFeedbackClicked);

        ambiguityFeedbackLayout.setOnClickListener(this::onAmbiguityFeedbackClicked);

        if(mDialog != null)
        {
            mDialog.setOnCancelListener(this::onCancelListener);
        }
    }

    public FeedBackDialog show()
    {
        if(mDialog != null && mContext != null)
        {
            initiateAllViews();
            initiateListeners();

            LayerDrawable layerDrawable = (LayerDrawable) mContext.getResources().getDrawable(R.drawable.reviewdialog_round_icon);
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.round_background);
            gradientDrawable.setColor(Color.parseColor("#FFFFFF"));
            layerDrawable.setDrawableByLayerId(R.id.round_background,gradientDrawable);

            Drawable drawable = this.mIcon;
            Drawable wrappedDrawable = DrawableCompat.wrap(drawable);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                DrawableCompat.setTint(drawable.mutate(), mContext.getResources().getColor(mIconColor));
            }
            else
            {
                drawable.setColorFilter(mContext.getResources().getColor(mIconColor), PorterDuff.Mode.SRC_IN);
            }


            layerDrawable.setDrawableByLayerId(R.id.drawable_image,drawable);

            titleImageView.setImageDrawable(layerDrawable);
            titleTextView.setText(this.mTitle);
            descriptionTextView.setText(this.mDescription);
            reviewQuestionTextView.setText(this.mReviewQuestion);

            positiveFeedbackTextView.setText(this.mPositiveFeedbackText);
            positiveFeedbackIconView.setImageDrawable(this.mPositiveFeedbackIcon);
            positiveFeedbackIconView.setColorFilter(mContext.getResources().getColor(mIconColor));

            negativeFeedbackTextView.setText(this.mNegativeFeedbackText);
            negativeFeedbackIconView.setImageDrawable(this.mNegativeFeedbackIcon);
            negativeFeedbackIconView.setColorFilter(mContext.getResources().getColor(mIconColor));

            ambiguityFeedbackTextView.setText(this.mAmbiguityFeedbackText);
            ambiguityFeedbackIconView.setImageDrawable(this.mAmbiguityFeedbackIcon);
            ambiguityFeedbackIconView.setColorFilter(mContext.getResources().getColor(mIconColor));

            feedbackBodyLayout.setBackgroundResource(this.mBackgroundColor);

            mDialog.show();
        }
        return this;
    }

    public Drawable getTitleIcon()
    {
        return mIcon;
    }

    public FeedBackDialog setIcon(Drawable mIcon)
    {
        this.mIcon =  mIcon;
        return this;
    }

    public String getTitle()
    {
        return mTitle;
    }

    public FeedBackDialog setTitle(String mTitle)
    {
        this.mTitle = mTitle;
        return this;
    }

    public String getDescription()
    {
        return mDescription;
    }

    public FeedBackDialog setDescription(String mDescription)
    {
        this.mDescription = mDescription;
        return this;
    }

    public String getPositiveFeedbackText()
    {
        return mPositiveFeedbackText;
    }

    public FeedBackDialog setPositiveFeedbackText( String mPositiveFeedbackText)
    {
        this.mPositiveFeedbackText = mPositiveFeedbackText;
        return this;
    }

    public Drawable getPositiveFeedbackIcon()
    {
        return mPositiveFeedbackIcon;
    }

    public FeedBackDialog setPositiveFeedbackIcon(Drawable mPositiveFeedbackIcon)
    {
        this.mPositiveFeedbackIcon = mPositiveFeedbackIcon;
        return this;
    }

    public String getNegativeFeedbackText()
    {
        return mNegativeFeedbackText;
    }

    public FeedBackDialog setNegativeFeedbackText(String mNegativeFeedbackText)
    {
        this.mNegativeFeedbackText = mNegativeFeedbackText;
        return this;
    }

    public Drawable getNegativeFeedbackIcon()
    {
        return mNegativeFeedbackIcon;
    }

    public FeedBackDialog setNegativeFeedbackIcon(Drawable mNegativeFeedbackIcon)
    {
        this.mNegativeFeedbackIcon = mNegativeFeedbackIcon;
        return this;
    }

    public String getAmbiguityFeedbackText()
    {
        return mAmbiguityFeedbackText;
    }

    public FeedBackDialog setAmbiguityFeedbackText(String mAmbiguityFeedbackText)
    {
        this.mAmbiguityFeedbackText = mAmbiguityFeedbackText;
        return this;
    }

    public Drawable getAmbiguityFeedbackIcon()
    {
        return mAmbiguityFeedbackIcon;
    }

    public FeedBackDialog setAmbiguityFeedbackIcon(Drawable mAmbiguityFeedbackIcon)
    {
        this.mAmbiguityFeedbackIcon = mAmbiguityFeedbackIcon;
        return this;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public FeedBackDialog setBackgroundColor(@ColorRes int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
        return this;
    }

    public int getIconColor()
    {
        return mIconColor;
    }

    public FeedBackDialog setIconColor(@ColorRes int mIconColor)
    {
        this.mIconColor = mIconColor;
        return this;
    }

    public String getReviewQuestion()
    {
        return mReviewQuestion;
    }

    public FeedBackDialog setReviewQuestion(String mReviewQuestion)
    {
        this.mReviewQuestion = mReviewQuestion;
        return this;
    }

    public FeedBackDialog setOnReviewClickListener(FeedBackActionsListeners reviewActionsListeners)
    {
        this.mReviewActionsListener = reviewActionsListeners;
        return this;
    }

    public void dismiss()
    {
        if(mDialog != null)
        {
            mDialog.dismiss();
        }
    }

    private void onPositiveFeedbackClicked(View view)
    {
        if(mReviewActionsListener != null)
        {
            mReviewActionsListener.onPositiveFeedback(this);
        }
    }

    private void onNegativeFeedbackClicked(View view)
    {
        if(mReviewActionsListener != null)
        {
            mReviewActionsListener.onNegativeFeedback(this);
        }
    }

    private void onAmbiguityFeedbackClicked(View view)
    {
        if(mReviewActionsListener != null)
        {
            mReviewActionsListener.onAmbiguityFeedback(this);
        }
    }

    private void onCancelListener(DialogInterface dialog)
    {
        if (mReviewActionsListener != null)
        {
            mReviewActionsListener.onCancelListener(dialog);
        }
    }
}