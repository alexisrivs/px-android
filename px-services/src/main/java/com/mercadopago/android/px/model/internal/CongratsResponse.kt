package com.mercadopago.android.px.model.internal

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.mercadopago.android.px.model.Instruction
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class CongratsResponse(
    @SerializedName("mpuntos") val score: Score? = null,
    @SerializedName("discounts") val discount: Discount? = null,
    @SerializedName("banner") val adBanner: AdBanner? = null,
    @SerializedName("expense_split") val moneySplit: MoneySplit? = null,
    @SerializedName("cross_selling") private val crossSellingList: List<CrossSelling>? = emptyList(),
    val viewReceipt: Button? = null,
    val customOrder: Boolean = false,
    private val paymentMethodsImages: Map<String?, String?>? = emptyMap(),
    val autoReturn: AutoReturn? = null,
    val redirectUrl: String? = null,
    val backUrl: String? = null,
    val primaryButton: Button? = null,
    val secondaryButton: Button? = null,
    val instructions: Instruction? = null,
    val operationInfo: OperationInfo? = null
) : Parcelable {

    fun getCrossSellings() = crossSellingList ?: emptyList()
    fun getPaymentMethodsImages() = paymentMethodsImages ?: emptyMap()

    @Parcelize
    data class Score internal constructor(
        val progress: Progress,
        val title: String,
        val action: Button
    ): Parcelable {

        @Parcelize
        data class Progress internal constructor(
            val percentage: Float,
            @SerializedName("level_color") val color: String?,
            @SerializedName("level_number") val level: Int
        ) : Parcelable
    }

    @Parcelize
    data class Discount internal constructor(
        val title: String,
        val subtitle: String?,
        val action: Button,
        @SerializedName("action_download") val actionDownload: DownloadApp?,
        val touchpoint: PXBusinessTouchpoint?,
        private val items: List<Item>?
    ): Parcelable {

        fun getItems() = items ?: emptyList()

        @Parcelize
        data class DownloadApp internal constructor(
            val title: String,
            val action: Button
        ): Parcelable

        @Parcelize
        data class Item internal constructor(
            val title: String,
            val subtitle: String?,
            val icon: String,
            val target: String,
            val campaignId: String?
        ): Parcelable
    }

    @Parcelize
    data class AdBanner internal constructor(
        @SerializedName("content") val content: Content
    ): Parcelable {

        @Parcelize
        data class Content(
            @SerializedName("image_url") val urlImage: String,
            @SerializedName("deeplink") val urlDeepLink: String,
            @SerializedName("destination_url") val urlDestination: String,
            @SerializedName("print_url") val urlPrint: String
        ): Parcelable
    }

    @Parcelize
    data class CrossSelling internal constructor(
        val title: String,
        val icon: String,
        val action: Button,
        val contentId: String
    ): Parcelable

    @Parcelize
    data class PXBusinessTouchpoint internal constructor(
        val id: String,
        val type: String,
        val content: @RawValue Map<*, *>?,
        val tracking: @RawValue Map<String, Any>?,
        val additionalEdgeInsets: AdditionalEdgeInsets?
    ): Parcelable

    @Parcelize
    data class AdditionalEdgeInsets internal constructor(
        val top: Int,
        val left: Int,
        val bottom: Int,
        val right: Int
    ): Parcelable

    @Parcelize
    data class MoneySplit internal constructor(
        val title: Text,
        val action: Button,
        val imageUrl: String
    ): Parcelable

    @Parcelize
    data class AutoReturn(
        val label: String,
        val seconds: Int
    ) : Parcelable

    @Parcelize
    data class OperationInfo(
        val hierarchy: String,
        val type: String,
        val body: String
    ) : Parcelable

    companion object {
        @JvmField val EMPTY = CongratsResponse()
    }
}
