package com.mercadopago.android.px.tracking.internal.model;

import android.os.Parcel;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.mercadopago.android.px.tracking.internal.BankInfoHelper;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@Keep
public class ConfirmData extends AvailableMethod {

    private final String reviewType;
    private int paymentMethodSelectedIndex;
    private String bankName;
    private String externalAccountId;

    public static final Creator<ConfirmData> CREATOR = new Creator<ConfirmData>() {
        @Override
        public ConfirmData createFromParcel(final Parcel in) {
            return new ConfirmData(in);
        }

        @Override
        public ConfirmData[] newArray(final int size) {
            return new ConfirmData[size];
        }
    };

    public static ConfirmData from(final String paymentTypeId, final String paymentMethodId, final boolean isCompliant,
                                   final boolean hasAdditionalInfoNeeded, @NonNull final BankInfoHelper bankInfoHelper) {
        final Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("has_payer_information", isCompliant);
        extraInfo.put("additional_information_needed", hasAdditionalInfoNeeded);
        return new ConfirmData(ReviewType.ONE_TAP, new AvailableMethod(paymentMethodId, paymentTypeId, extraInfo), bankInfoHelper);
    }

    public ConfirmData(@NonNull final ReviewType reviewType, final int paymentMethodSelectedIndex,
                       @NonNull final AvailableMethod availableMethod, @NonNull final BankInfoHelper bankInfoHelper) {
        super(availableMethod.paymentMethodId, availableMethod.paymentMethodType, availableMethod.extraInfo);
        this.reviewType = reviewType.value;
        this.paymentMethodSelectedIndex = paymentMethodSelectedIndex;
        this.bankName = bankInfoHelper.getBankName(availableMethod.paymentMethodId);
        this.externalAccountId = bankInfoHelper.getExternalAccountId(availableMethod.paymentMethodId);
    }

    public ConfirmData(@NonNull final ReviewType reviewType,
        @NonNull final AvailableMethod availableMethod, @NonNull final BankInfoHelper bankInfoHelper) {
        super(availableMethod.paymentMethodId, availableMethod.paymentMethodType, availableMethod.extraInfo);
        this.reviewType = reviewType.value;
        this.bankName = bankInfoHelper.getBankName(availableMethod.paymentMethodId);
        this.externalAccountId = bankInfoHelper.getExternalAccountId(availableMethod.paymentMethodId);
    }

    @SuppressWarnings("WeakerAccess")
    protected ConfirmData(final Parcel in) {
        super(in);
        reviewType = in.readString();
        paymentMethodSelectedIndex = in.readInt();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(reviewType);
        dest.writeInt(paymentMethodSelectedIndex);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public enum ReviewType {
        ONE_TAP("one_tap"),
        TRADITIONAL("traditional");

        public final String value;

        ReviewType(@NonNull final String value) {
            this.value = value;
        }
    }
}
