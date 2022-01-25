package kr.co.skchurch.seokwangyouthdoor.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.utils.Util

@Entity(tableName = "calendar")
data class CalendarEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var title: String,
    var detailInfo: String? = null,
    var date: String,
    var scheduleType: Int = Constants.SCHEDULE_TYPE_EVENT,
    override var uuid: String,
    override var timeStamp: String
): BaseEntity {
    constructor():this(null, "", null, "", Constants.SCHEDULE_TYPE_EVENT, Util.getUUID(), Util.getTimestamp())
}
