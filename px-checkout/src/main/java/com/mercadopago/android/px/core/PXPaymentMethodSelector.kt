package com.mercadopago.android.px.core

import android.app.Activity
import com.mercadopago.android.px.configuration.AdvancedConfiguration
import com.mercadopago.android.px.configuration.PaymentConfiguration
import com.mercadopago.android.px.configuration.DynamicDialogConfiguration
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration
import com.mercadopago.android.px.configuration.TrackingConfiguration
import com.mercadopago.android.px.configuration.CustomStringConfiguration
import com.mercadopago.android.px.configuration.ScheduledPaymentMethodType
import com.mercadopago.android.px.core.internal.NoOpPaymentProcessor
import com.mercadopago.android.px.internal.core.ProductIdProvider
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule

class PXPaymentMethodSelector private constructor(builder: Builder) {

    val accessToken = builder.accessToken
    val publicKey = builder.publicKey
    val preferenceId = builder.preferenceId
    val dynamicDialogConfiguration = builder.dynamicDialogConfiguration
    val customStringConfiguration = builder.customStringConfiguration
    val discountParamsConfiguration = builder.discountParamsConfiguration
    val trackingConfiguration = builder.trackingConfiguration
    val scheduledPaymentMethodType = builder.scheduledPaymentMethodType
    val acceptThirdPartyCard = builder.acceptThirdPartyCard
    val charges = builder.charges
    val productId = builder.productId

    fun start(activity: Activity, requestCode: Int) {
        val advancedConfiguration = AdvancedConfiguration
            .Builder()
            .setAcceptThirdPartyCard(acceptThirdPartyCard)
            .setCustomStringConfiguration(customStringConfiguration)
            .setDiscountParamsConfiguration(discountParamsConfiguration)
            .setDynamicDialogConfiguration(dynamicDialogConfiguration)
            .setProductId(productId)
            .build()

        val paymentProcessor = PaymentConfiguration
            .Builder()
            .addChargeRules(charges)
            .setSupportSplit(true)
            .build()

        MercadoPagoCheckout.Builder(publicKey, preferenceId, paymentProcessor)
            .setAdvancedConfiguration(advancedConfiguration)
            .setPrivateKey(accessToken)
            .setTrackingConfiguration(trackingConfiguration)
            .setScheduledPaymentMethodsConfiguration(scheduledPaymentMethodType)
            .build()
            .startPayment(activity, requestCode)
    }

    class Builder(val publicKey: String, val preferenceId: String) {

        internal lateinit var accessToken: String
        internal var dynamicDialogConfiguration = DynamicDialogConfiguration.Builder().build()
        internal var customStringConfiguration = CustomStringConfiguration.Builder().build()
        internal var discountParamsConfiguration = DiscountParamsConfiguration.Builder().build()
        internal var trackingConfiguration = TrackingConfiguration.Builder().build()
        internal var scheduledPaymentMethodType = ScheduledPaymentMethodType.NON_SCHEDULED
        internal var acceptThirdPartyCard = false
        internal var charges: List<PaymentTypeChargeRule> = emptyList()
        internal var productId: String = ProductIdProvider.DEFAULT_PRODUCT_ID

        /**
         * Private key provides save card capabilities and account money balance.
         *
         * @param accessToken the user private key
         * @return builder to keep operating
         */
        fun setAccessToken(accessToken: String) = apply { this.accessToken = accessToken }

        /**
         * It provides support for custom checkout functionality/ configure special behaviour You can enable/disable
         * several functionality.
         *
         * @param dynamicDialogConfiguration your configuration.
         * @return builder to keep operating
         */
        fun setDynamicDialogConfiguration(dynamicDialogConfiguration: DynamicDialogConfiguration) = apply {
            this.dynamicDialogConfiguration = dynamicDialogConfiguration
        }

        /**
         * It provides support for custom checkout functionality/ configure special behaviour You can enable/disable
         * several functionality.
         *
         * @param customStringConfiguration your configuration.
         * @return builder to keep operating
         */
        fun setCustomStringConfiguration(customStringConfiguration: CustomStringConfiguration) = apply {
            this.customStringConfiguration = customStringConfiguration
        }

        /**
         * It provides support for custom checkout functionality/ configure special behaviour You can enable/disable
         * several functionality.
         *
         * @param discountParamsConfiguration your configuration.
         * @return builder to keep operating
         */
        fun setDiscountParamsConfiguration(discountParamsConfiguration: DiscountParamsConfiguration) = apply {
            this.discountParamsConfiguration = discountParamsConfiguration
        }

        /**
         * It provides support for third party card
         *
         * @param supportThirdPartyCard your configuration.
         * @return builder to keep operating
         */
        fun setSupportThirdPartyCard(supportThirdPartyCard: Boolean) = apply {
            this.acceptThirdPartyCard = supportThirdPartyCard
        }

        /**
         * It provides support for custom checkout functionality/ configure special behaviour You can enable/disable
         * several functionality.
         *
         * @param productId your configuration.
         * @return builder to keep operating
         */
        fun setProductId(productId: String) = apply {
            this.productId = productId
        }

        /**
         * Add extra charges that will apply to total amount.
         *
         * @param charges the list of charges that could apply.
         * @return builder to keep operating
         */
        fun setChargeRules(charges: List<PaymentTypeChargeRule>) = apply {
            this.charges = charges
        }

        /**
         * It provides additional configurations to modify tracking and session data.
         *
         * @param trackingConfiguration your configuration.
         * @return builder to keep operating
         */
        fun setTrackingConfiguration(trackingConfiguration: TrackingConfiguration) = apply {
            this.trackingConfiguration = trackingConfiguration
        }

        /**
         * Provides additional settings to modify the types of payment methods to display at checkout.
         * These can be programmable payment methods (SCHEDULED), non-programmable payment methods (NON_SCHEDULED),
         * or all (ALL).
         *
         * @param scheduledPaymentMethodType your configuration.
         * @return builder to keep operating
         */
        fun setScheduledPaymentMethodsConfiguration(scheduledPaymentMethodType: ScheduledPaymentMethodType) = apply {
            this.scheduledPaymentMethodType = scheduledPaymentMethodType
        }

        /**
         * @return [PXPaymentMethodSelector] instance
         */
        fun build() : PXPaymentMethodSelector {
            check(accessToken != null) { "Access token is required" }
            return PXPaymentMethodSelector(this)
        }
    }
}