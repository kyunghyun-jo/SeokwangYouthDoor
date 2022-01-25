package kr.co.skchurch.seokwangyouthdoor.ui.board

import android.os.Handler
import android.os.Looper
import com.orhanobut.logger.Logger
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.skchurch.seokwangyouthdoor.data.*
import kr.co.skchurch.seokwangyouthdoor.data.entities.BoardEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.FreeBoardEntity
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class BoardViewModel : ViewModel() {

    companion object {
        private val TAG = BoardViewModel::class.java.simpleName
    }

    private val _boardListData = MutableLiveData<List<BoardEntity>>()
    val boardListData: LiveData<List<BoardEntity>> = _boardListData
    private var db: AppDatabase = AppDatabase.getDatabase()
    //var list: List<BoardEntity>? = null
    var boardMutableList = mutableListOf<BoardEntity>()

    private val _freeBoardListData = MutableLiveData<List<FreeBoardEntity>>()
    val freeBoardListData: LiveData<List<FreeBoardEntity>> = _freeBoardListData
    //var freeBoardList: List<FreeBoardEntity>? = null
    var freeBoardMutableList = mutableListOf<FreeBoardEntity>()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            requestFreeBoardDB()
        }
    }

    private var isReadyForUseFirebase = false
    private fun requestFreeBoardDB() {
        freeBoardMutableList = mutableListOf()
        if(FirebaseManager.instance.getCurrentUserId() == FirebaseConstants.EMPTY_USER) {
            freeBoardMutableList.addAll(db.freeBoardDao().getAllData())
            Handler(Looper.getMainLooper()).post(Runnable {
                _freeBoardListData.value = freeBoardMutableList.toList()
            })
            return
        }
        isReadyForUseFirebase = false

        FirebaseManager.instance.registerFreeBoardDB(object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {
                isReadyForUseFirebase = true
                if(snapshot.childrenCount > 0L) {
                    val entity = snapshot.getValue(FreeBoardEntity::class.java)
                    if(entity == null || freeBoardMutableList.contains(entity)) {
                        return
                    }
                    Logger.d("requestFreeBoardDB onDataChange entity : $entity")
                    freeBoardMutableList.add(entity)
                    Thread(Runnable {
                        db.freeBoardDao().insertData(entity)
                    }).start()
                    freeBoardMutableList.sortByDescending { it.timeStamp }
                    Logger.d("requestFreeBoardDB freeBoardMutableList : $freeBoardMutableList")
                    //db.timetableDao().insertData(entity)
                    _freeBoardListData.value = freeBoardMutableList.toList()
                }
            }

            override fun onValueCancelled(error: DatabaseError) {
                isReadyForUseFirebase = true
            }

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
            }

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
                Logger.d("onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val freeBoardEntity = snapshot.getValue(FreeBoardEntity::class.java)!!
                var foundedIndex = -1
                freeBoardMutableList.forEachIndexed { index, entity ->
                    if(entity.uuid == freeBoardEntity.uuid) {
                        foundedIndex = index
                    }
                }
                if(foundedIndex == -1) return
                //firebaseFreeBoardDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                freeBoardMutableList.removeAt(foundedIndex)
                freeBoardMutableList.add(foundedIndex, freeBoardEntity)
                _freeBoardListData.postValue(freeBoardMutableList.toList())
            }

            override fun onEventChildRemoved(snapshot: DataSnapshot) {
                isReadyForUseFirebase = true
            }

            override fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
            }

            override fun onEventCancelled(error: DatabaseError) {
                isReadyForUseFirebase = true
            }

        })

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            Logger.d("Firebase check isReadyForUseFirebase : $isReadyForUseFirebase")
            if(isReadyForUseFirebase) return@Runnable
            requestCurrentFreeBoardData()
        }, Constants.NETWORK_CHECK_DELAY)
    }

    /*
    fun requestDataByClassName(className: String) = GlobalScope.launch(Dispatchers.IO) {
        tempList.clear()
        tempList.addAll(db.boardDao().getDataByClassName(className))
        //Logger.d("requestDataByClassName list : $tempList")
        if (tempList.isNotEmpty()) {
            list = tempList.toList()
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = list
        })
    }
     */

    private var isReadyForUseFirebase2 = false
    fun requestDataByClassName(className: String) = GlobalScope.launch(Dispatchers.IO) {
        boardMutableList = mutableListOf()
        if(FirebaseManager.instance.getCurrentUserId() == FirebaseConstants.EMPTY_USER) {
            boardMutableList.addAll(db.boardDao().getAllData())
            Handler(Looper.getMainLooper()).post(Runnable {
                _boardListData.value = boardMutableList.toList()
            })
            return@launch
        }
        isReadyForUseFirebase = false
        FirebaseManager.instance.registerClassBoardDB(className, object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {
                isReadyForUseFirebase2 = true
                if(snapshot.childrenCount > 0L) {
                    val entity = snapshot.getValue(BoardEntity::class.java)
                    Logger.d("onDataChange entity : $entity")
                    if(entity!=null && !boardMutableList.contains(entity)) {
                        boardMutableList.add(entity)
                        Thread(Runnable {
                            db.boardDao().insertData(entity)
                        }).start()
                        _boardListData.value = boardMutableList.toList()
                    }
                }
            }

            override fun onValueCancelled(error: DatabaseError) {
                isReadyForUseFirebase2 = true
            }

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase2 = true
                Logger.d("onEventChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
            }

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase2 = true
                Logger.d("onEventChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                /*
                val boardEntity = snapshot.getValue(BoardEntity::class.java)!!
                var foundedIndex = -1
                boardMutableList.forEachIndexed { index, entity ->
                    if(entity.uuid == boardEntity.uuid) {
                        foundedIndex = index
                    }
                }
                if(foundedIndex == -1) return
                //firebaseBoardDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                boardMutableList.removeAt(foundedIndex)
                boardMutableList.add(foundedIndex, boardEntity)
                _boardListData.postValue(boardMutableList.toList())
                 */
            }

            override fun onEventChildRemoved(snapshot: DataSnapshot) {
                isReadyForUseFirebase2 = true
            }

            override fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase2 = true
            }

            override fun onEventCancelled(error: DatabaseError) {
                isReadyForUseFirebase2 = true
            }

        })

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            Logger.d("Firebase check isReadyForUseFirebase : $isReadyForUseFirebase2")
            if(isReadyForUseFirebase2) return@Runnable
            requestCurrentBoardData(className)
        }, Constants.NETWORK_CHECK_DELAY)
    }

    private fun getLastBoardId(): Long {
        var lastId = 0L
        boardMutableList.forEach {
            if(it.id!! > lastId) lastId = it.id!!
        }
        return lastId
    }

    private fun getLastFreeBoardId(): Long {
        var lastId = 0L
        freeBoardMutableList.forEach {
            if(it.id!! > lastId) lastId = it.id!!
        }
        return lastId
    }

    private fun requestCurrentBoardData(className: String) {
        Logger.d("requestCurrentBoardData : $className")
        GlobalScope.launch(Dispatchers.IO) {
            boardMutableList.clear()
            db.boardDao().getAllData().forEach {
                if(it.className == className) {
                    boardMutableList.add(it)
                }
            }
            //boardMutableList.sortByDescending { it.timeStamp }
            //boardMutableList.addAll(db.boardDao().getAllData())
            Handler(Looper.getMainLooper()).post(Runnable {
                _boardListData.value = boardMutableList.toList()
            })
        }
    }

    fun requestCurrentFreeBoardData() {
        GlobalScope.launch(Dispatchers.IO) {
            freeBoardMutableList.clear()
            freeBoardMutableList.addAll(db.freeBoardDao().getAllData())
            //freeBoardMutableList.sortByDescending { it.timeStamp }
            Handler(Looper.getMainLooper()).post(Runnable {
                _freeBoardListData.value = freeBoardMutableList.toList()
            })
        }
    }

    fun addBoard(entity: BoardEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.add(entity)
        val user = mutableMapOf<String, Any>()
        user[FirebaseConstants.KEY_ID] = getLastBoardId()+1
        user[FirebaseConstants.KEY_TITLE] = entity.title
        user[FirebaseConstants.KEY_AUTHOR] = entity.author
        user[FirebaseConstants.KEY_DESCRIPTION] = entity.description
        user[FirebaseConstants.KEY_IMAGE_URL] = entity.imageUrl.orEmpty()
        user[FirebaseConstants.KEY_CLASSNAME] = entity.className.orEmpty()
        user[FirebaseConstants.KEY_UUID] = Util.getUUID()
        user[FirebaseConstants.KEY_TIMESTAMP] = Util.getTimestamp()
        FirebaseManager.instance.classBoardDB?.child("${FirebaseConstants.KEY_ITEM}${getLastBoardId()}")?.updateChildren(user)

        boardMutableList.clear()
        db.boardDao().insertData(entity)
        boardMutableList.addAll(db.boardDao().getAllData())
        //Handler(Looper.getMainLooper()).post(Runnable {
        //    _boardListData.value = boardMutableList.toList()
        //})
    }

    fun editBoard(entity: BoardEntity) = GlobalScope.launch(Dispatchers.IO) {
        var foundedIndex = -1
        boardMutableList.forEachIndexed { index, it ->
            if(foundedIndex!=-1) return@forEachIndexed
            if(entity.uuid == it.uuid) {
                foundedIndex = index
            }
        }
        val user = mutableMapOf<String, Any>()
        user[FirebaseConstants.KEY_TITLE] = entity.title
        user[FirebaseConstants.KEY_DESCRIPTION] = entity.description
        user[FirebaseConstants.KEY_IMAGE_URL] = entity.imageUrl.orEmpty()
        user[FirebaseConstants.KEY_TIMESTAMP] = Util.getTimestamp()
        Logger.d("editBoard firebaseBoardDB : ${FirebaseManager.instance.classBoardDB} / foundedIndex : $foundedIndex")
        FirebaseManager.instance.classBoardDB?.child("${FirebaseConstants.KEY_ITEM}${foundedIndex}")?.updateChildren(user)

        boardMutableList.removeAt(foundedIndex)
        boardMutableList.add(foundedIndex, entity)
        db.boardDao().insertData(entity)
        //boardMutableList.addAll(db.boardDao().getAllData())
        //Handler(Looper.getMainLooper()).post(Runnable {
        //    _boardListData.value = boardMutableList.toList()
        //})
    }

    fun removeBoard(entity: BoardEntity) = GlobalScope.launch(Dispatchers.IO) {
        /*
        var foundedIndex = -1
        boardMutableList.forEachIndexed { index, boardEntity ->
            if(foundedIndex!=-1) return@forEachIndexed
            if(entity.uuid == boardEntity.uuid) {
                foundedIndex = index
            }
        }
         */
        val itemId = entity.id!!.toInt()-1
        Logger.d("removeBoard itemId : $itemId")
        FirebaseManager.instance.classBoardDB?.child("${FirebaseConstants.KEY_ITEM}$itemId")?.removeValue()

        boardMutableList.clear()
        db.boardDao().deleteDataByUUID(entity.uuid)
        boardMutableList.addAll(db.boardDao().getAllData())
        Handler(Looper.getMainLooper()).post(Runnable {
            _boardListData.value = boardMutableList.toList()
        })
    }

    fun addFreeBoard(entity: FreeBoardEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.add(entity)
        val user = mutableMapOf<String, Any>()
        user[FirebaseConstants.KEY_ID] = getLastFreeBoardId()+1
        user[FirebaseConstants.KEY_TITLE] = entity.title
        user[FirebaseConstants.KEY_AUTHOR] = entity.author
        user[FirebaseConstants.KEY_DESCRIPTION] = entity.description
        user[FirebaseConstants.KEY_IMAGE_URL] = entity.imageUrl.orEmpty()
        user[FirebaseConstants.KEY_UUID] = Util.getUUID()
        user[FirebaseConstants.KEY_TIMESTAMP] = Util.getTimestamp()
        FirebaseManager.instance.freeBoardDB.child("${FirebaseConstants.KEY_ITEM}${getLastFreeBoardId()}").updateChildren(user)

        freeBoardMutableList.clear()
        db.freeBoardDao().insertData(entity)
        freeBoardMutableList.addAll(db.freeBoardDao().getAllData())
        //Handler(Looper.getMainLooper()).post(Runnable {
        //    _freeBoardListData.value = freeBoardMutableList.toList()
        //})
    }

    fun removeFreeBoard(entity: FreeBoardEntity) = GlobalScope.launch(Dispatchers.IO) {
        /*
        var foundedIndex = -1
        freeBoardMutableList.forEachIndexed { index, boardEntity ->
            if(foundedIndex!=-1) return@forEachIndexed
            if(entity.uuid == boardEntity.uuid) {
                foundedIndex = index
            }
        }
         */
        val itemId = entity.id!!.toInt()-1
        Logger.d("removeFreeBoard itemId : $itemId")
        FirebaseManager.instance.freeBoardDB?.child("${FirebaseConstants.KEY_ITEM}$itemId")?.removeValue()

        freeBoardMutableList.clear()
        db.freeBoardDao().deleteDataByUUID(entity.uuid)
        freeBoardMutableList.addAll(db.freeBoardDao().getAllData())
        Handler(Looper.getMainLooper()).post(Runnable {
            _freeBoardListData.value = freeBoardMutableList.toList()
        })
    }

    /*
    fun refreshFreeBoard() = GlobalScope.launch(Dispatchers.IO) {
        Logger.d("refreshFreeBoard 111 : ${db.freeBoardDao().getAllData()}")
        freeBoardMutableList.clear()
        freeBoardMutableList.addAll(db.freeBoardDao().getAllData())
        Logger.d("refreshFreeBoard 222 : $freeBoardMutableList")
        Handler(Looper.getMainLooper()).post(Runnable {
            _freeBoardListData.value = freeBoardMutableList.toList()
        })
    }
     */
}