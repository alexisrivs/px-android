package com.mercadopago.android.px.internal.features.one_tap.slider;

import androidx.annotation.NonNull;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorModelByApplication;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.viewmodel.DisabledPaymentMethodDescriptorModel;
import com.mercadopago.android.px.internal.viewmodel.GoingToModel;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.model.internal.Application;
import java.util.List;

public class PaymentMethodHeaderAdapter
    extends HubableAdapter<List<PaymentMethodDescriptorModelByApplication>, PaymentMethodHeaderView> {

    private static final int NO_SELECTED = -1;

    private int currentIndex = NO_SELECTED;

    public PaymentMethodHeaderAdapter(@NonNull final PaymentMethodHeaderView view) {
        super(view);
    }

    @Override
    public void showInstallmentsList() {
        view.showInstallmentsListTitle();
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState, @NonNull final Application application) {
        this.currentIndex = currentIndex;
        final PaymentMethodDescriptorModelByApplication paymentMethodDescriptorModelByApplication =
            data.get(currentIndex);
        paymentMethodDescriptorModelByApplication.update(application);
        final PaymentMethodDescriptorView.Model currentModel = paymentMethodDescriptorModelByApplication.getCurrent();
        view.updateData(currentModel.hasPayerCostList(), currentModel instanceof DisabledPaymentMethodDescriptorModel);
        configureVisibilityTitle(application.getPaymentMethod().getType(),
                                 (splitSelectionState.userWantsToSplit() ||
                                 currentModel instanceof DisabledPaymentMethodDescriptorModel),
                                 !currentModel.hasPayerCostList());
    }

    private void configureVisibilityTitle(@NonNull final String paymentMethod, final boolean titleVisibleDebitCard,
        final boolean titleVisibleCreditCard) {
        if (paymentMethod.equals("debit_card"))
            view.changeVisibilityTitle(titleVisibleDebitCard);
        else
            view.changeVisibilityTitle(titleVisibleCreditCard);
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        final GoingToModel goingTo = position < currentIndex ? GoingToModel.BACKWARDS : GoingToModel.FORWARD;
        final int nextIndex = goingTo == GoingToModel.BACKWARDS ? currentIndex - 1 : currentIndex + 1;
        if (nextIndex >= 0 && nextIndex < data.size()) {
            final PaymentMethodDescriptorView.Model currentModel = data.get(currentIndex).getCurrent();
            final PaymentMethodDescriptorView.Model nextModel = data.get(nextIndex).getCurrent();
            final PaymentMethodHeaderView.Model viewModel =
                new PaymentMethodHeaderView.Model(goingTo, currentModel.hasPayerCostList(),
                    nextModel.hasPayerCostList());
            view.trackPagerPosition(positionOffset, viewModel);
        }
    }

    @Override
    public List<PaymentMethodDescriptorModelByApplication> getNewModels(final HubAdapter.Model model) {
        return model.paymentMethodDescriptorModels;
    }
}