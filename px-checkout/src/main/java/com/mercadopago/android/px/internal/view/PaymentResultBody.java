package com.mercadopago.android.px.internal.view;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mercadolibre.android.andesui.message.AndesMessage;
import com.mercadolibre.android.mlbusinesscomponents.components.actioncard.MLBusinessActionCardView;
import com.mercadolibre.android.mlbusinesscomponents.components.actioncard.MLBusinessActionCardViewData;
import com.mercadolibre.android.mlbusinesscomponents.components.adbanner.MLBusinessAdBannerData;
import com.mercadolibre.android.mlbusinesscomponents.components.adbanner.MLBusinessAdBannerView;
import com.mercadolibre.android.mlbusinesscomponents.components.common.dividingline.MLBusinessDividingLineView;
import com.mercadolibre.android.mlbusinesscomponents.components.common.downloadapp.MLBusinessDownloadAppData;
import com.mercadolibre.android.mlbusinesscomponents.components.common.downloadapp.MLBusinessDownloadAppView;
import com.mercadolibre.android.mlbusinesscomponents.components.crossselling.MLBusinessCrossSellingBoxData;
import com.mercadolibre.android.mlbusinesscomponents.components.crossselling.MLBusinessCrossSellingBoxView;
import com.mercadolibre.android.mlbusinesscomponents.components.discount.MLBusinessDiscountBoxView;
import com.mercadolibre.android.mlbusinesscomponents.components.loyalty.MLBusinessLoyaltyRingData;
import com.mercadolibre.android.mlbusinesscomponents.components.loyalty.MLBusinessLoyaltyRingView;
import com.mercadolibre.android.mlbusinesscomponents.components.loyalty.broadcaster.LoyaltyBroadcastData;
import com.mercadolibre.android.mlbusinesscomponents.components.loyalty.broadcaster.LoyaltyBroadcaster;
import com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.callback.OnClickCallback;
import com.mercadolibre.android.mlbusinesscomponents.components.touchpoint.view.MLBusinessTouchpointView;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.business_result.CongratsViewModel;
import com.mercadopago.android.px.internal.features.business_result.PXDiscountBoxData;
import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentCongratsResponse;
import com.mercadopago.android.px.internal.util.FragmentUtil;
import com.mercadopago.android.px.internal.util.Logger;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.ExternalFragment;
import com.mercadopago.android.px.services.MercadoPagoBannerService;

import java.util.List;

import static com.mercadopago.android.px.internal.util.MercadoPagoUtil.isMP;
import static com.mercadopago.android.px.internal.util.MercadoPagoUtil.isMPInstalled;

public final class PaymentResultBody extends LinearLayout {

    private static final String TAG = "";

    public interface Listener extends MLBusinessLoyaltyRingView.OnClickLoyaltyRing,
        OnClickCallback, MLBusinessDownloadAppView.OnClickDownloadApp,
        MLBusinessCrossSellingBoxView.OnClickCrossSellingBoxView,
        MLBusinessDiscountBoxView.OnClickDiscountBox, MLBusinessAdBannerView.OnClickAdBannerView {

        void onClickShowAllDiscounts(@NonNull final String deepLink);

        void onClickViewReceipt(@NonNull final String deeLink);

        void onClickTouchPoint(@Nullable String deepLink);

        void onClickMoneySplit();

        @Override
        default void onClick(@Nullable final String deepLink) {
            onClickTouchPoint(deepLink);
        }
    }

    public PaymentResultBody(final Context context) {
        this(context, null);
    }

    public PaymentResultBody(final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaymentResultBody(final Context context, @Nullable final AttributeSet attrs,
        final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initView(@NonNull final Model model) {
        final int layout = model.congratsViewModel.getCustomOrder() ?
            R.layout.px_payment_result_body_custom_order : R.layout.px_payment_result_body;
        inflate(getContext(), layout, this);
        setOrientation(VERTICAL);
    }

    public void init(@NonNull final Model model, @NonNull final Listener listener) {
        initView(model);
        renderFragment(R.id.px_fragment_container_important, model.importantFragment);
        renderLoyalty(model.congratsViewModel.getLoyaltyRingData(), listener);
        renderDiscounts(model.congratsViewModel.getDiscountBoxData(), listener);
        renderAdBanner(model.congratsViewModel.getAdBannerData(), listener);
        renderShowAllDiscounts(model.congratsViewModel.getShowAllDiscounts(), listener);
        renderDownload(model.congratsViewModel.getDownloadAppData(), listener);
        renderMoneySplit(model.congratsViewModel.getActionCardViewData(), listener);
        renderCrossSellingBox(model.congratsViewModel.getCrossSellingBoxData(), listener);
        renderReceipt(model.receiptId);
        renderOperationInfo(model.congratsViewModel.getOperationInfo());
        renderHelp(model.help);
        renderFragment(R.id.px_fragment_container_top, model.topFragment);
        renderMethods(model);
        renderViewReceipt(model.congratsViewModel.getViewReceipt(), listener);
        renderFragment(R.id.px_fragment_container_bottom, model.bottomFragment);
    }

    private void renderOperationInfo(@Nullable final PaymentCongratsResponse.OperationInfo operationInfo) {
        final AndesMessage operationInfoView = findViewById(R.id.operationInfo);
        if (operationInfo != null) {
            operationInfoView.setVisibility(VISIBLE);
            operationInfoView.setBody(operationInfo.getBody());
            operationInfoView.setHierarchy(operationInfo.getHierarchy());
            operationInfoView.setType(operationInfo.getType());
        } else {
            operationInfoView.setVisibility(GONE);
        }
    }

    private void renderLoyalty(@Nullable final MLBusinessLoyaltyRingData loyaltyData,
        @NonNull final Listener onClickLoyaltyRing) {
        final MLBusinessLoyaltyRingView loyaltyView = findViewById(R.id.loyaltyView);
        final MLBusinessDividingLineView dividingView = findViewById(R.id.dividingLineView);

        if (loyaltyData != null) {
            loyaltyView.init(loyaltyData, onClickLoyaltyRing);
            sendLoyaltyInfoToBroadcaster(loyaltyData);
        } else {
            loyaltyView.setVisibility(GONE);
            dividingView.setVisibility(GONE);
        }
    }

    private void renderAdBanner(@Nullable final MLBusinessAdBannerData adBannerData, @NonNull final Listener onClickAdBanner){
        final MLBusinessAdBannerView adBannerView = findViewById(R.id.adBannerView);
        MercadoPagoBannerService mercadoPagoBannerService = new MercadoPagoBannerService(getContext());
        mercadoPagoBannerService.sendPrint("9%2FIXYnP3owBpTPMD7jEwJx5vrhRIbx8dbVmDIEHpWI8gp3SKlHdTXoLetytKegnYqd%2BBJ6ipNVIiyF0oCczQNehj2CRk9%2Bwjk7NJogxLsbMqxvrqcQt3m53iTEFJdrvcOgRZJ%2FF6SHX569%2F%2Fg7%2FSZE8%2BpFNOkRr9IZ7J7W2JYFZXd8PN2e8uoImzYktV6LgsS1oKoBJqrvM%2FBKIX2ZRowUNmPicPF%2F%2BOfVj7JnpFuEnm6W6uLpS1JeW2rIzQx3xo%2FzcmLtpnWFdj0%2BHxuf%2FGL1bcB0JFSaOGfQgaF%2FmnyOJ3y0RtUYBx36XoLuq9rzAH9OGAMerdkyvcTXpjBNSG9W%2B0P4ro6FHyIGvgwZ%2BtLJa2j6p8iXJC3fCgW7pPQ1HG%2FRa742Xd%2Bx4ukTeovbMCmLD6");
        if(adBannerData != null) {
            adBannerView.init(adBannerData, onClickAdBanner);
        } else {
            adBannerView.setVisibility(GONE);
        }

        adBannerView.setOnClickListener(v -> {
            mercadoPagoBannerService.sendClick("FlkALdG%2FIV5aiKwOEyxyquQ%2FVEZpw%2FD1slWmUIDlUjMNpx%2Beo6qytI0NWglb%2BGzN2Mm8iQclZXstohYI59oCoHlwfS3Lw9DV0G1H%2FlVTJaHaeinHKjAOpldJM48U4W%2BT1aV19JEQ1gnzke%2FxWy3OTbkaWiZv%2BY6bJC7EzVkJNZe7%2B89LoB0ANOOU3Tdb0sTr8QNUMX9VhF%2FGBLntLE%2Fhe%2Feujg1%2FU%2Fz6F266t4AHbZ2qDAEYVZpIcN%2By4gIA7R3gBpMDGlGi0xrWtQ%2F8Tf7PKnIejlK05SzNhSHmCr%2FrSf2ANjIYGDLXpNbzG6N1AilW9EzW%2Fvaf");
            onClickAdBanner.onClickAdBannerViewLink(adBannerData.getUrlDeepLink());
        });
    }

    private void sendLoyaltyInfoToBroadcaster(MLBusinessLoyaltyRingData loyaltyData) {
        LoyaltyBroadcastData loyaltyBroadcastData = new LoyaltyBroadcastData();

        loyaltyBroadcastData.setLevel(loyaltyData.getRingNumber());
        loyaltyBroadcastData.setPercentage(loyaltyData.getRingPercentage());
        loyaltyBroadcastData.setPrimaryColor(loyaltyData.getRingHexaColor());

        LoyaltyBroadcaster.getInstance().updateInfo(getContext(), loyaltyBroadcastData);
    }

    private void renderShowAllDiscounts(@Nullable final PaymentCongratsResponse.Action showAllDiscountAction,
        @NonNull final Listener onClickDiscountBox) {
        final MeliButton showAllDiscounts = findViewById(R.id.showAllDiscounts);

        if (showAllDiscountAction != null && isMPInstalled(getContext().getPackageManager())) {
            showAllDiscounts.setText(showAllDiscountAction.getLabel());
            showAllDiscounts.setOnClickListener(
                v -> onClickDiscountBox.onClickShowAllDiscounts(showAllDiscountAction.getTarget()));
        } else {
            showAllDiscounts.setVisibility(GONE);
        }
    }

    private void renderDiscounts(@Nullable final PXDiscountBoxData discountData,
        @NonNull final Listener onClickDiscountBox) {
        final MLBusinessTouchpointView touchpointView = findViewById(R.id.touchpointView);
        final TextView touchpointLabel = findViewById(R.id.touchpointLabelView);
        final MLBusinessDiscountBoxView discountBoxView = findViewById(R.id.discountView);
        final MLBusinessDividingLineView dividingView = findViewById(R.id.dividingLineView);

        if (discountData != null && discountData.getTouchpoint() != null) {
            if (!TextUtil.isEmpty(discountData.getTitle())) {
                touchpointLabel.setText(discountData.getTitle());
                touchpointLabel.setVisibility(VISIBLE);
            }
            touchpointView.setVisibility(VISIBLE);
            touchpointView.setCanOpenMercadoPago(isMPInstalled(getContext().getPackageManager()));
            touchpointView.setOnClickCallback(onClickDiscountBox);
            touchpointView.setTracker(discountData.getTracker());
            touchpointView.init(discountData.getTouchpoint());
        } else if (discountData != null && !discountData.getDiscountBoxData().getItems().isEmpty()) {
            discountBoxView.setVisibility(VISIBLE);
            discountBoxView.init(discountData.getDiscountBoxData(), onClickDiscountBox);
        } else {
            dividingView.setVisibility(GONE);
        }
    }

    private void renderMoneySplit(@Nullable final MLBusinessActionCardViewData actionCardViewData,
        @NonNull final Listener listener) {
        final MLBusinessActionCardView moneySplitView = findViewById(R.id.money_split_view);
        if (actionCardViewData != null && isMP(getContext())) {
            moneySplitView.setVisibility(VISIBLE);
            moneySplitView.init(actionCardViewData);
            moneySplitView.setOnClickListener(v -> listener.onClickMoneySplit());
        } else {
            moneySplitView.setVisibility(GONE);
        }
    }

    private void renderDownload(@Nullable final MLBusinessDownloadAppData downloadAppData,
        @NonNull final Listener listener) {
        final MLBusinessDownloadAppView downloadAppView = findViewById(R.id.downloadView);
        if (downloadAppData != null && !isMPInstalled(getContext().getPackageManager())) {
            downloadAppView.init(downloadAppData, listener);
        } else {
            downloadAppView.setVisibility(GONE);
        }
    }

    private void renderCrossSellingBox(
        @NonNull final List<MLBusinessCrossSellingBoxData> crossSellingBoxDataList,
        @NonNull final Listener listener) {

        final LinearLayout businessComponents = findViewById(R.id.businessComponents);

        for (final MLBusinessCrossSellingBoxData crossSellingData : crossSellingBoxDataList) {
            final MLBusinessCrossSellingBoxView crossSellingBoxView =
                new MLBusinessCrossSellingBoxView(getContext());
            crossSellingBoxView.init(crossSellingData, listener);
            businessComponents.addView(crossSellingBoxView);
        }
    }

    private void renderReceipt(@Nullable final String receiptId) {
        final PaymentResultReceipt receipt = findViewById(R.id.receipt);
        if (TextUtil.isNotEmpty(receiptId)) {
            receipt.setVisibility(VISIBLE);
            receipt.setReceiptId(receiptId);
        } else {
            receipt.setVisibility(GONE);
        }
    }

    private void renderHelp(@Nullable final String help) {
        final View helpContainer = findViewById(R.id.help);
        if (TextUtil.isNotEmpty(help)) {
            helpContainer.setVisibility(VISIBLE);
            final TextView helpTitle = helpContainer.findViewById(R.id.help_title);
            final TextView helpDescription = helpContainer.findViewById(R.id.help_description);
            helpTitle.setText(R.string.px_what_can_do);
            helpDescription.setText(help);
        } else {
            helpContainer.setVisibility(GONE);
        }
    }

    private void renderMethods(@NonNull final Model model) {
        final PaymentResultMethod primaryMethod = findViewById(R.id.primaryMethod);
        final PaymentResultMethod secondaryMethod = findViewById(R.id.secondaryMethod);

        primaryMethod.setVisibility(GONE);
        secondaryMethod.setVisibility(GONE);

        if (model.methodModels != null && !model.methodModels.isEmpty()) {
            if (model.methodModels.size() > 1) {
                secondaryMethod.setVisibility(VISIBLE);
                secondaryMethod.setModel(model.methodModels.get(1));
            }
            primaryMethod.setVisibility(VISIBLE);
            primaryMethod.setModel(model.methodModels.get(0));
        }
    }

    private void renderViewReceipt(@Nullable final PaymentCongratsResponse.Action viewReceiptAction, final Listener listener) {
        final MeliButton viewReceiptButton = findViewById(R.id.view_receipt_action);
        final String target = viewReceiptAction != null ? viewReceiptAction.getTarget() : null;
        if (TextUtil.isNotEmpty(target) && isMPInstalled(getContext().getPackageManager())) {
            viewReceiptButton
                .setBackground(ContextCompat.getDrawable(getContext(), R.drawable.px_quiet_button_selector));
            viewReceiptButton.setText(viewReceiptAction.getLabel());
            viewReceiptButton.setOnClickListener(
                v -> listener.onClickViewReceipt(target));
        } else {
            viewReceiptButton.setVisibility(GONE);
        }
    }

    private void renderFragment(@IdRes final int id, @Nullable final ExternalFragment fragment) {
        final ViewGroup container = findViewById(id);
        if (fragment != null) {
            container.setVisibility(VISIBLE);
            FragmentUtil.replaceFragment(container, fragment);
        } else {
            container.setVisibility(GONE);
        }
    }

    public static final class Model {
        /* default */ final List<PaymentResultMethod.Model> methodModels;
        /* default */ final CongratsViewModel congratsViewModel;
        @Nullable /* default */ final String receiptId;
        @Nullable /* default */ final String help;
        @Nullable /* default */ final String statement;
        @Nullable /* default */ final ExternalFragment topFragment;
        @Nullable /* default */ final ExternalFragment bottomFragment;
        @Nullable /* default */ final ExternalFragment importantFragment;

        public Model(@NonNull final Builder builder) {
            methodModels = builder.methodModels;
            congratsViewModel = builder.congratsViewModel;
            receiptId = builder.receiptId;
            help = builder.help;
            statement = builder.statement;
            topFragment = builder.topFragment;
            bottomFragment = builder.bottomFragment;
            importantFragment = builder.importantFragment;
        }

        public static class Builder {
            /* default */ List<PaymentResultMethod.Model> methodModels;
            /* default */ CongratsViewModel congratsViewModel;
            @Nullable /* default */ String receiptId;
            @Nullable /* default */ String help;
            @Nullable /* default */ String statement;
            @Nullable /* default */ ExternalFragment topFragment;
            @Nullable /* default */ ExternalFragment bottomFragment;
            @Nullable /* default */ ExternalFragment importantFragment;

            public Builder setMethodModels(@NonNull final List<PaymentResultMethod.Model> methodModels) {
                this.methodModels = methodModels;
                return this;
            }

            public Builder setCongratsViewModel(@NonNull final CongratsViewModel congratsViewModel) {
                this.congratsViewModel = congratsViewModel;
                return this;
            }

            public Builder setReceiptId(@Nullable final String receiptId) {
                this.receiptId = receiptId;
                return this;
            }

            public Builder setHelp(@Nullable final String help) {
                this.help = help;
                return this;
            }

            public Builder setTopFragment(@Nullable final ExternalFragment topFragment) {
                this.topFragment = topFragment;
                return this;
            }

            public Builder setBottomFragment(@Nullable final ExternalFragment bottomFragment) {
                this.bottomFragment = bottomFragment;
                return this;
            }

            public Builder setImportantFragment(@Nullable final ExternalFragment importantFragment) {
                this.importantFragment = importantFragment;
                return this;
            }

            public Builder setStatement(@Nullable final String statement) {
                this.statement = statement;
                return this;
            }

            public Model build() {
                return new Model(this);
            }
        }
    }
}