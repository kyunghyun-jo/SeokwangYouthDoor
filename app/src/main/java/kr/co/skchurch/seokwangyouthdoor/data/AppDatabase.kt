package kr.co.skchurch.seokwangyouthdoor.data

import android.content.Context
import com.orhanobut.logger.Logger
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kr.co.skchurch.seokwangyouthdoor.data.dao.*
import kr.co.skchurch.seokwangyouthdoor.data.entities.*

@Database(
    entities = [
        HomeEntity::class,
        MemberInfoEntity::class,
        TimetableEntity::class,
        CalendarEntity::class,
        BoardEntity::class,
        FreeBoardEntity::class,
        SimpleEntity::class
               ],
    version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun homeDao(): HomeDao
    abstract fun memberInfoDao(): MemberInfoDao
    abstract fun timetableDao(): TimetableDao
    abstract fun calendarDao(): CalendarDao
    abstract fun boardDao(): BoardDao
    abstract fun freeBoardDao(): FreeBoardDao
    abstract fun memberCategoryDao(): MemberCategoryDao

    companion object {
        var context: Context? = null
        private var instance: AppDatabase? = null
        fun getDatabase(): AppDatabase {
            if(context == null) throw Exception()
            if(instance == null) {
                instance = Room.databaseBuilder(
                    context!!,
                    AppDatabase::class.java,
                    "seokwangYouth.db")
                    .build()
            }
            return instance!!
        }
    }
}