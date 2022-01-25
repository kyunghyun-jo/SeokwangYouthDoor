package kr.co.skchurch.seokwangyouthdoor.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kr.co.skchurch.seokwangyouthdoor.utils.Util

@Parcelize
@Entity(tableName = "simple")
data class SimpleEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var title: String? = null,
    var value: String? = null,
    var imageUrl: String? = null,
    override var uuid: String,
    override var timeStamp: String
): BaseEntity, Parcelable {
    constructor():this(null, null, null, null, Util.getUUID(), Util.getTimestamp())
}
