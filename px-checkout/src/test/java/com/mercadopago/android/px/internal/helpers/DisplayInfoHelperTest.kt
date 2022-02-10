package com.mercadopago.android.px.internal.helpers

import com.mercadopago.android.px.internal.features.payment_congrats.model.PaymentInfo
import com.mercadopago.android.px.internal.features.payment_result.model.DisplayInfoHelper
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.model.CustomSearchItem
import com.mercadopago.android.px.model.PaymentData
import com.mercadopago.android.px.model.PaymentMethod
import com.mercadopago.android.px.model.display_info.BankTransferDisplayInfo
import com.mercadopago.android.px.model.display_info.DisplayInfo
import com.mercadopago.android.px.model.display_info.ResultInfo
import com.mercadopago.android.px.model.internal.Text
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkClass
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val CUSTOM_OPTION_ID = "123"
private const val DESCRIPTION_TITLE = "Debito inmediato"
private const val BANK_NAME = "Banco Ciudad"
private const val CBU = "CBU: ***4412"
private const val ICON_URL = "http://ddd.png"
private const val RESULT_INFO_TITLE = "result_info_title"
private const val RESULT_INFO_SUBTITLE = "result_info_subtitle"
private const val BACKGROUND_COLOR = "#000000"
private const val TEXT_COLOR = "#FFFFFF"
private const val WEIGHT = "semi_bold"

class DisplayInfoHelperTest {

    @MockK
    private lateinit var payerPaymentMethodRepository: PayerPaymentMethodRepository
    @MockK
    private lateinit var userSelectionRepository: UserSelectionRepository

    private lateinit var displayInfoHelper: DisplayInfoHelper

    @Before
    fun `set up`() {
        MockKAnnotations.init(this)
        displayInfoHelper = DisplayInfoHelper(payerPaymentMethodRepository, userSelectionRepository)
    }

    @Test
    fun `when payment method is DEBIN then return texts details from display info in payment info`() {
        val payerPaymentMethod = mockkClass(CustomSearchItem::class)
        val paymentData = mockkClass(PaymentData::class)
        val paymentInfoBuilder = PaymentInfo.Builder()
        val title = mockkClass(Text::class) {
            every { message } returns DESCRIPTION_TITLE
            every { backgroundColor } returns BACKGROUND_COLOR
            every { textColor } returns TEXT_COLOR
            every { weight } returns WEIGHT
        }
        val bankName = mockkClass(Text::class) {
            every { message } returns BANK_NAME
            every { backgroundColor } returns BACKGROUND_COLOR
            every { textColor } returns TEXT_COLOR
            every { weight } returns WEIGHT
        }
        val cbu = mockkClass(Text::class) {
            every { message } returns CBU
            every { backgroundColor } returns BACKGROUND_COLOR
            every { textColor } returns TEXT_COLOR
            every { weight } returns WEIGHT
        }

        val bankTransferDisplayInfoResultPaymentMethod = mockkClass(BankTransferDisplayInfo.Result.PaymentMethod::class) {
            every { detail } returns listOf(title, bankName, cbu)
            every { iconUrl } returns ICON_URL
        }
        val bankTransferDisplayInfoResult = mockkClass(BankTransferDisplayInfo.Result::class) {
            every { paymentMethod } returns bankTransferDisplayInfoResultPaymentMethod
        }
        val bankTransferDisplayInfo = mockkClass(BankTransferDisplayInfo::class) {
            every { result } returns bankTransferDisplayInfoResult
        }

        every { payerPaymentMethod.bankTransferDisplayInfo } returns bankTransferDisplayInfo
        every { userSelectionRepository.customOptionId } returns CUSTOM_OPTION_ID
        every { payerPaymentMethodRepository[CUSTOM_OPTION_ID] } returns payerPaymentMethod

        displayInfoHelper.resolve(paymentData, paymentInfoBuilder)

        val paymentInfo = paymentInfoBuilder.build()

        assertEquals(DESCRIPTION_TITLE, paymentInfo.descriptionText!!.message)
        assertEquals(BACKGROUND_COLOR, paymentInfo.descriptionText!!.backgroundColor)
        assertEquals(TEXT_COLOR, paymentInfo.descriptionText!!.textColor)
        assertEquals(WEIGHT, paymentInfo.descriptionText!!.weight)

        assertEquals(BANK_NAME, paymentInfo.paymentMethodDescriptionText!!.message)
        assertEquals(BACKGROUND_COLOR, paymentInfo.paymentMethodDescriptionText!!.backgroundColor)
        assertEquals(TEXT_COLOR, paymentInfo.paymentMethodDescriptionText!!.textColor)
        assertEquals(WEIGHT, paymentInfo.paymentMethodDescriptionText!!.weight)

        assertEquals(CBU, paymentInfo.statementText!!.message)
        assertEquals(BACKGROUND_COLOR, paymentInfo.statementText!!.backgroundColor)
        assertEquals(TEXT_COLOR, paymentInfo.statementText!!.textColor)
        assertEquals(WEIGHT, paymentInfo.statementText!!.weight)
    }

    @Test
    fun `when payment method is not DEBIN then return texts details in payment info`() {
        val payerPaymentMethod = mockkClass(CustomSearchItem::class)
        val paymentInfoBuilder = PaymentInfo.Builder()
        val descriptionTitle = mockkClass(Text::class) {
            every { message } returns DESCRIPTION_TITLE
            every { backgroundColor } returns BACKGROUND_COLOR
            every { textColor } returns TEXT_COLOR
            every { weight } returns WEIGHT
        }
        val resultInfoMock = mockkClass(ResultInfo::class) {
            every { title } returns RESULT_INFO_TITLE
            every { subtitle } returns RESULT_INFO_SUBTITLE
        }
        val displayInfoMock = mockkClass(DisplayInfo::class) {
            every { description } returns descriptionTitle
            every { resultInfo } returns resultInfoMock
        }
        val paymentMethodMock = mockkClass(PaymentMethod::class) {
            every { displayInfo } returns displayInfoMock
        }
        val paymentData = mockkClass(PaymentData::class) {
            every { paymentMethod } returns paymentMethodMock
        }

        every { userSelectionRepository.customOptionId } returns CUSTOM_OPTION_ID
        every { payerPaymentMethodRepository[CUSTOM_OPTION_ID] } returns payerPaymentMethod
        every { payerPaymentMethod.bankTransferDisplayInfo } returns null

        displayInfoHelper.resolve(paymentData, paymentInfoBuilder)

        val paymentInfo = paymentInfoBuilder.build()

        assertEquals(RESULT_INFO_TITLE, paymentInfo.consumerCreditsInfo!!.title)
        assertEquals(RESULT_INFO_SUBTITLE, paymentInfo.consumerCreditsInfo!!.subtitle)

        assertEquals(DESCRIPTION_TITLE, paymentInfo.paymentMethodDescriptionText!!.message)
        assertEquals(BACKGROUND_COLOR, paymentInfo.paymentMethodDescriptionText!!.backgroundColor)
        assertEquals(TEXT_COLOR, paymentInfo.paymentMethodDescriptionText!!.textColor)
        assertEquals(WEIGHT, paymentInfo.paymentMethodDescriptionText!!.weight)
    }
}
