package kr.co.skchurch.seokwangyouthdoor.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kr.co.skchurch.seokwangyouthdoor.utils.Util

@Parcelize
@Entity(tableName = "board")
data class BoardEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var title: String,
    var author: String,
    var description: String,
    var className: String? = null,
    var imageUrl: String? = null,
    override var uuid: String,
    override var timeStamp: String
): BaseEntity, Parcelable {
    constructor(): this(null, "", "", "", null, null, Util.getUUID(), Util.getTimestamp())
}
