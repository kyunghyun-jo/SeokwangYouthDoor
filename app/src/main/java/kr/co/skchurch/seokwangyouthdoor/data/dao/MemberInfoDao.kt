package kr.co.skchurch.seokwangyouthdoor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity

@Dao
interface MemberInfoDao {
    @Query("SELECT * FROM memberInfo")
    fun getAllData(): List<MemberInfoEntity>

    @Query("SELECT * FROM memberInfo ORDER BY name ASC")
    fun getAllDataOrderByName(): List<MemberInfoEntity>

    @Query("SELECT * FROM memberInfo ORDER BY birth ASC")
    fun getAllDataOrderByBirth(): List<MemberInfoEntity>

    //@Query("SELECT * FROM memberInfo ORDER BY name ASC")
    //fun getAllDataOrderByName(searchText: String?): List<MemberInfoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: MemberInfoEntity)

    @Query("SELECT * FROM memberInfo WHERE id = :id")
    fun getDataById(id: Long): MemberInfoEntity

    @Query("SELECT * FROM memberInfo WHERE name = :name")
    fun getDataByName(name: String): List<MemberInfoEntity>

    @Query("DELETE FROM memberInfo WHERE id = :id")
    fun deleteDataById(id: Long)

    @Query("DELETE FROM memberInfo WHERE name = :name")
    fun deleteDataByName(name: String)

    @Query("DELETE FROM memberInfo")
    fun deleteAllData()

    @Query("SELECT * FROM memberInfo WHERE uuid = :uuid")
    fun getDataByUUID(uuid: String): MemberInfoEntity

    @Query("DELETE FROM memberInfo WHERE uuid = :uuid")
    fun deleteDataByUUID(uuid: String)
}