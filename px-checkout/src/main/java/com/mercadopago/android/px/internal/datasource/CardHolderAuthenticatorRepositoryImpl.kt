package com.mercadopago.android.px.internal.datasource

import android.util.MalformedJsonException
import com.mercadopago.android.px.addons.model.ThreeDSDataOnlyParams
import com.mercadopago.android.px.internal.model.CardHolderAuthenticatorBody
import com.mercadopago.android.px.internal.repository.CardHolderAuthenticatorRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.services.CardHolderAuthenticatorService
import com.mercadopago.android.px.internal.util.JsonUtil
import com.mercadopago.android.px.internal.util.TextUtil
import com.mercadopago.android.px.model.PaymentData

class CardHolderAuthenticatorRepositoryImpl(
    private val cardHolderAuthenticatorService: CardHolderAuthenticatorService,
    private val paymentSettingRepository: PaymentSettingRepository) : CardHolderAuthenticatorRepository {

    override suspend fun authenticate(paymentData: PaymentData, threeDSDataOnlyParams: ThreeDSDataOnlyParams?): Any {
        val token = paymentData.token ?: throw IllegalStateException("Missing token during authentication")
        val accessToken = paymentSettingRepository.privateKey ?: return TextUtil.EMPTY
        val threeDSParams = threeDSDataOnlyParams ?: throw IllegalStateException("Missing ThreeDS SDK Data")
        val body = CardHolderAuthenticatorBody(
            paymentData.rawAmount.toString(),
            CardHolderAuthenticatorBody.Card(
                token.cardHolder?.name.orEmpty(),
                paymentData.paymentMethod.id
            ),
            paymentSettingRepository.currency,
            paymentSettingRepository.site.id,
            threeDSParams.sdkAppId,
            threeDSParams.deviceData,
            JsonUtil.fromJson(threeDSParams.sdkEphemeralPublicKey, CardHolderAuthenticatorBody.SdkEphemPubKey::class.java)
                ?: throw MalformedJsonException("Malformed sdkEphemeralPublicKey"),
            threeDSParams.sdkReferenceNumber,
            threeDSParams.sdkTransactionId
        )
        return cardHolderAuthenticatorService.authenticate(token.id, accessToken, body)
    }
}
