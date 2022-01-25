package kr.co.skchurch.seokwangyouthdoor.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kr.co.skchurch.seokwangyouthdoor.utils.Util

@Entity(tableName = "home")
data class HomeEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,
    var type: Int = 0,
    var title: String? = null,
    var value: String? = null,
    var imageUrl: String? = null,
    var flagNew: Int = 0,
    override var uuid: String,
    override var timeStamp: String
): BaseEntity {
    constructor(): this(null, 0, null, null, null, 0, Util.getUUID(), Util.getTimestamp())
    //constructor(id: Long?, type: Int, title: String?, imageUrl: String?, imageDrawableId: Int?, isNew: Boolean):
    //        this(null, 0, null, null, null, if(isNew) 1 else 0)
}
