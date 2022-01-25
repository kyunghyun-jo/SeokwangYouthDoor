package kr.co.skchurch.seokwangyouthdoor.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kr.co.skchurch.seokwangyouthdoor.utils.Util

@Entity(tableName = "timeTable")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var title: String,
    var middleValue: String? = null,
    var lastValue: String,
    override var uuid: String,
    override var timeStamp: String
): BaseEntity {
    constructor(): this(null, "", null, "", Util.getUUID(), Util.getTimestamp())
}
