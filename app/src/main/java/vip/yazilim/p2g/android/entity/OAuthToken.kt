package vip.yazilim.p2g.android.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * @author mustafaarifsisman - 26.01.2020
 * @contact mustafaarifsisman@gmail.com
 */
data class OAuthToken(
    var userId: String?,
    var accessToken: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(accessToken)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OAuthToken> {
        override fun createFromParcel(parcel: Parcel): OAuthToken {
            return OAuthToken(parcel)
        }

        override fun newArray(size: Int): Array<OAuthToken?> {
            return arrayOfNulls(size)
        }
    }
}