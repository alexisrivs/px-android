package com.mercadopago.android.px.tracking.internal

import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.model.PaymentMethods

internal class BankInfoHelper(
    val payerPaymentMethodRepository: PayerPaymentMethodRepository,
    val userSelectionRepository: UserSelectionRepository
) {
    fun getExternalAccountId(paymentMethodId: String): String? {
        val customOptionId = userSelectionRepository.customOptionId
        return if (customOptionId != null && paymentMethodId == PaymentMethods.ARGENTINA.DEBIN) {
            payerPaymentMethodRepository[customOptionId]?.id
        } else {
            null
        }
    }

    fun getBankName(paymentMethodId: String): String? {
        val customOptionId = userSelectionRepository.customOptionId
        return if (customOptionId != null && paymentMethodId == PaymentMethods.ARGENTINA.DEBIN) {
            payerPaymentMethodRepository[customOptionId]?.bankInfo?.name
        } else {
            null
        }
    }
}
