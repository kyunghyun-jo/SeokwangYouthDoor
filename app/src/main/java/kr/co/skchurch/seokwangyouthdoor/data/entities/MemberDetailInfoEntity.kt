package kr.co.skchurch.seokwangyouthdoor.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kr.co.skchurch.seokwangyouthdoor.utils.Util

@Parcelize
@Entity(tableName = "memberDetailInfo")
data class MemberDetailInfoEntity(
    @PrimaryKey
    var email: String?,
    var age: Int?,
    var school: String? = null,
    var snsId: String? = null,
    var hobby: String? = null,
    var isLeader: Boolean? = false,
    var address: String? = null
): Parcelable {
    constructor(): this(null, null, null, null, null, false, null)
}
