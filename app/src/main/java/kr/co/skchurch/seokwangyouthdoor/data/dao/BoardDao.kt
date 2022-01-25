package kr.co.skchurch.seokwangyouthdoor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.co.skchurch.seokwangyouthdoor.data.entities.BoardEntity

@Dao
interface BoardDao {
    @Query("SELECT * FROM board ORDER BY id DESC")
    fun getAllData(): List<BoardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: BoardEntity)

    @Query("SELECT * FROM board WHERE id = :id")
    fun getDataById(id: Long): BoardEntity

    @Query("SELECT * FROM board WHERE className = :className ORDER BY id DESC")
    fun getDataByClassName(className: String): List<BoardEntity>

    @Query("SELECT * FROM board WHERE author = :author")
    fun getDataByAuthor(author: String): List<BoardEntity>

    @Query("DELETE FROM board WHERE id = :id")
    fun deleteDataById(id: Long)

    @Query("DELETE FROM board WHERE author = :author")
    fun deleteDataByAuthor(author: String)

    @Query("DELETE FROM board")
    fun deleteAllData()

    @Query("SELECT * FROM board WHERE uuid = :uuid")
    fun getDataByUUID(uuid: String): BoardEntity

    @Query("DELETE FROM board WHERE uuid = :uuid")
    fun deleteDataByUUID(uuid: String)
}