package com.mercadopago.android.px.tracking.internal.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.internal.mappers.Mapper;
import com.mercadopago.android.px.internal.repository.ApplicationSelectionRepository;
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository;
import com.mercadopago.android.px.model.AccountMoneyMetadata;
import com.mercadopago.android.px.model.BenefitsMetadata;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethods;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.internal.Application;
import com.mercadopago.android.px.model.internal.OneTapItem;
import com.mercadopago.android.px.tracking.internal.events.BankTransferExtraInfo;
import com.mercadopago.android.px.tracking.internal.model.AccountMoneyExtraInfo;
import com.mercadopago.android.px.tracking.internal.model.AvailableMethod;
import com.mercadopago.android.px.tracking.internal.model.CardExtraExpress;
import com.mercadopago.android.px.tracking.internal.model.CreditsExtraInfo;
import com.mercadopago.android.px.tracking.internal.model.PayerCostInfo;
import java.util.Set;

public class FromSelectedExpressMetadataToAvailableMethods extends Mapper<OneTapItem, AvailableMethod> {

    @NonNull private final FromApplicationToApplicationInfo fromApplicationToApplicationInfo;
    @NonNull private final ApplicationSelectionRepository applicationSelectionRepository;
    @NonNull private final PayerPaymentMethodRepository payerPaymentMethodRepository;
    @NonNull private final Set<String> cardsWithEsc;
    @Nullable private final PayerCost selectedPayerCost;
    private final boolean isSplit;

    public FromSelectedExpressMetadataToAvailableMethods(
        @NonNull final ApplicationSelectionRepository applicationSelectionRepository,
        @NonNull final FromApplicationToApplicationInfo fromApplicationToApplicationInfo,
        @NonNull final PayerPaymentMethodRepository payerPaymentMethodRepository,
        @NonNull final Set<String> cardsWithEsc,
        @Nullable final PayerCost selectedPayerCost, final boolean isSplit) {
        this.applicationSelectionRepository = applicationSelectionRepository;
        this.fromApplicationToApplicationInfo = fromApplicationToApplicationInfo;
        this.payerPaymentMethodRepository = payerPaymentMethodRepository;
        this.cardsWithEsc = cardsWithEsc;
        this.selectedPayerCost = selectedPayerCost;
        this.isSplit = isSplit;
    }

    @Override
    public AvailableMethod map(@NonNull final OneTapItem oneTapItem) {
        boolean hasInterestFree = false;
        boolean hasReimbursement = false;
        final BenefitsMetadata benefits = oneTapItem.getBenefits();

        if (benefits != null) {
            hasInterestFree = benefits.getInterestFree() != null;
            hasReimbursement = benefits.getReimbursement() != null;
        }

        final Application.PaymentMethod paymentMethod =
            applicationSelectionRepository.get(oneTapItem).getPaymentMethod();

        final AvailableMethod.Builder builder = new AvailableMethod.Builder(
            paymentMethod.getId(),
            paymentMethod.getType(),
            hasInterestFree, hasReimbursement, fromApplicationToApplicationInfo.map(oneTapItem.getApplications()));

        if (PaymentTypes.isCardPaymentType(paymentMethod.getType())) {
            final CardMetadata card = oneTapItem.getCard();
            builder.setExtraInfo(CardExtraExpress.selectedExpressSavedCard(card, selectedPayerCost,
                cardsWithEsc.contains(card.getId()), isSplit).toMap());
        } else if (PaymentTypes.isAccountMoney(paymentMethod.getType()) && oneTapItem.isAccountMoney()) {
            final AccountMoneyMetadata accountMoney = oneTapItem.getAccountMoney();
            builder
                .setExtraInfo(new AccountMoneyExtraInfo(accountMoney.getBalance(), accountMoney.isInvested()).toMap());
        } else if (PaymentMethods.CONSUMER_CREDITS.equals(paymentMethod.getType()) && selectedPayerCost != null) {
            builder.setExtraInfo(new CreditsExtraInfo(new PayerCostInfo(selectedPayerCost)).toMap());
        } else if (oneTapItem.isBankTransfer() && oneTapItem.getPaymentMethodId().equals(PaymentMethods.ARGENTINA.DEBIN)) {
            builder.setExtraInfo(
                    new BankTransferExtraInfo(oneTapItem.getBankTransfer().getId(), payerPaymentMethodRepository).toMap()
            );
        }

        return builder.build();
    }
}
