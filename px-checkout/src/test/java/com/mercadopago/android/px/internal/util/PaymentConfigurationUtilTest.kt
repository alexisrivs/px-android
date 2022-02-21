package com.mercadopago.android.px.internal.util

import com.mercadopago.android.px.assertEquals
import com.mercadopago.android.px.configuration.PaymentConfiguration
import com.mercadopago.android.px.core.v2.PaymentProcessor
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class PaymentConfigurationUtilTest {

    @Mock
    private lateinit var paymentConfiguration: PaymentConfiguration

    @Mock
    private lateinit var processor: PaymentProcessor

    @Test
    fun getPaymentProcessorShouldReturnProcessorV2() {
        whenever(paymentConfiguration.paymentProcessorV2).thenReturn(processor)
        PaymentConfigurationUtil.getPaymentProcessor(paymentConfiguration)?.assertEquals(processor)
    }
}