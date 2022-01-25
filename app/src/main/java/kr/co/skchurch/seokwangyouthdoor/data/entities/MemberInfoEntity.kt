package kr.co.skchurch.seokwangyouthdoor.data.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kr.co.skchurch.seokwangyouthdoor.data.MemberType
import kr.co.skchurch.seokwangyouthdoor.utils.Util

@Parcelize
@Entity(tableName = "memberInfo")
data class MemberInfoEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var name: String,
    var gender: Int,
    var birth: String,
    var type: Int,
    var phoneNumber: String? = null,
    var className: String? = null,
    var imageUrl: String? = null,
    @Embedded
    var detailInfo: MemberDetailInfoEntity? = null,
    override var uuid: String,
    override var timeStamp: String
): BaseEntity, Parcelable {
    constructor():this(null, "", 0, "", 0, null, null, null, null, Util.getUUID(), Util.getTimestamp())
}
