package kr.co.skchurch.seokwangyouthdoor.data

import com.orhanobut.logger.Logger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.entities.*
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class FirebaseManager {
    companion object {
        private val TAG = FirebaseManager::class.java.simpleName
        val instance by lazy {
            FirebaseManager()
        }
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userDB by lazy {
        Firebase.database.reference.child(FirebaseConstants.TABLE_USERS)
    }

    private lateinit var commonDB: DatabaseReference
    private var commonDBCallback: IFirebaseCallback? = null

    fun getUserName(): String {
        var userName = auth.currentUser!!.displayName
        if(userName == null || userName.isEmpty())
            userName = auth.currentUser!!.email
        if(userName == null || userName.isEmpty())
            userName = SeokwangYouthApplication.context!!.getString(R.string.unknownUser)
        return userName
    }

    fun registerCommonDB(callback: IFirebaseCallback) {
        if(!::commonDB.isInitialized) commonDB = userDB.child(FirebaseConstants.TABLE_COMMON_DATA)
        commonDBCallback = callback
        commonDB.child(FirebaseConstants.KEY_PRAISE_LINK).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[CommonDB]onDataChange PRAISE_LINK snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                if(Constants.IS_TEST_MODE) {
                    if(snapshot.childrenCount == 0L) {
                        if(snapshot.value == null) {
                            val user = mutableMapOf<String, Any>()
                            val testData = generateTestPraiseLink()
                            user[FirebaseConstants.KEY_PRAISE_LINK] = testData
                            commonDB.updateChildren(user)
                        }
                    }
                }
                commonDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[CommonDB]onCancelled error : $error")
                commonDBCallback?.onValueCancelled(error)
            }

        })

        commonDB.child(FirebaseConstants.KEY_PRAISE_LINK).addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CommonDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CommonDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                commonDBCallback?.onEventChildChanged(snapshot, previousChildName)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[CommonDB]onChildRemoved snapshot : $snapshot")
                commonDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CommonDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                commonDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[CommonDB]onCancelled error : $error")
                commonDBCallback?.onEventCancelled(error)
            }

        })

        commonDB.child(FirebaseConstants.KEY_WORSHIP_NOTICE).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[CommonDB]onDataChange WORSHIP_NOTICE snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                if(Constants.IS_TEST_MODE) {
                    if(snapshot.childrenCount == 0L) {
                        if(snapshot.value == null) {
                            val user = mutableMapOf<String, Any>()
                            user[FirebaseConstants.KEY_WORSHIP_NOTICE] = generateTestWorshipNotice()
                            commonDB.updateChildren(user)
                        }
                    }
                }
                commonDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[CommonDB]onCancelled error : $error")
                commonDBCallback?.onValueCancelled(error)
            }

        })

        commonDB.child(FirebaseConstants.KEY_WORSHIP_NOTICE).addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CommonDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CommonDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                commonDBCallback?.onEventChildChanged(snapshot, previousChildName)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[CommonDB]onChildRemoved snapshot : $snapshot")
                commonDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CommonDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                commonDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[CommonDB]onCancelled error : $error")
                commonDBCallback?.onEventCancelled(error)
            }

        })
    }

    private lateinit var homeDB: DatabaseReference
    private var homeDBCallback: IFirebaseCallback? = null
    
    fun registerHomeDB(callback: IFirebaseCallback) {
        if(!::homeDB.isInitialized) homeDB = userDB.child(FirebaseConstants.TABLE_HOME)
        homeDBCallback = callback
        homeDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[HomeDB]onDataChange snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                if(Constants.IS_TEST_MODE) {
                    if (snapshot.childrenCount == 0L) {
                        val homeList = generateTestHomeData()
                        homeList.forEachIndexed { index, item ->
                            val user = mutableMapOf<String, Any>()
                            user[FirebaseConstants.KEY_ID] = item.id!!
                            user[FirebaseConstants.KEY_TYPE] = item.type
                            user[FirebaseConstants.KEY_TITLE] = item.title.orEmpty()
                            user[FirebaseConstants.KEY_IMAGE_URL] = item.imageUrl.orEmpty()
                            user[FirebaseConstants.KEY_VALUE] = item.value.orEmpty()
                            user[FirebaseConstants.KEY_IS_NEW] = item.flagNew
                            user[FirebaseConstants.KEY_UUID] = item.uuid
                            user[FirebaseConstants.KEY_TIMESTAMP] = item.timeStamp
                            homeDB.child("${FirebaseConstants.KEY_ITEM}$index").updateChildren(user)
                        }
                    }
                }
                //homeDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[HomeDB]onCancelled error : $error")
                homeDBCallback?.onValueCancelled(error)
            }

        })

        homeDB.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[HomeDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key?.isNotEmpty() == true) {
                    getHomeDataByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[HomeDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                //homeDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                homeDBCallback?.onEventChildChanged(snapshot, previousChildName)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[HomeDB]onChildRemoved snapshot : $snapshot")
                homeDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[HomeDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                homeDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[HomeDB]onCancelled error : $error")
                homeDBCallback?.onEventCancelled(error)
            }

        })
    }

    private fun getHomeDataByKey(key: String) {
        Logger.d("[HomeDB]getHomeDataByKey key : $key")
        homeDB.child(key).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val entity = snapshot.getValue(HomeEntity::class.java)
                Logger.d("[HomeDB]onDataChange entity : $entity")
                homeDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.w("[HomeDB]onCancelled : $error")
                homeDBCallback?.onValueCancelled(error)
            }

        })
    }

    private lateinit var timetableDB: DatabaseReference
    private var timetableDBCallback: IFirebaseCallback? = null

    fun registerTimetableDB(callback: IFirebaseCallback) {
        if(!::timetableDB.isInitialized) timetableDB = userDB.child(FirebaseConstants.TABLE_TIMETABLE)
        timetableDBCallback = callback
        timetableDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[TimetableDB]onDataChange snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                if(Constants.IS_TEST_MODE) {
                    if (snapshot.childrenCount == 0L) {
                        val timeTableList = generateTestTimetableData()
                        timeTableList.forEachIndexed { index, item ->
                            val user = mutableMapOf<String, Any>()
                            user[FirebaseConstants.KEY_ID] = item.id!!
                            user[FirebaseConstants.KEY_TITLE] = item.title
                            user[FirebaseConstants.KEY_LAST_VALUE] = item.lastValue
                            user[FirebaseConstants.KEY_MIDDLE_VALUE] = item.middleValue.orEmpty()
                            user[FirebaseConstants.KEY_UUID] = item.uuid
                            user[FirebaseConstants.KEY_TIMESTAMP] = item.timeStamp
                            timetableDB.child("${FirebaseConstants.KEY_ITEM}$index")
                                .updateChildren(user)
                        }
                    }
                }
                //timetableDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[TimetableDB]onCancelled error : $error")
                timetableDBCallback?.onValueCancelled(error)
            }

        })

        timetableDB.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[TimetableDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key?.isNotEmpty() == true) {
                    getTimetableDataByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[TimetableDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                //timetableDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                timetableDBCallback?.onEventChildChanged(snapshot, previousChildName)
                notifyAllDataChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[TimetableDB]onChildRemoved snapshot : $snapshot")
                timetableDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[TimetableDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                timetableDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[TimetableDB]onCancelled error : $error")
                timetableDBCallback?.onEventCancelled(error)
            }

        })
    }

    private fun getTimetableDataByKey(key: String) {
        timetableDB.child(key).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val entity = snapshot.getValue(TimetableEntity::class.java)
                Logger.d("[TimetableDB]onDataChange entity : $entity")
                timetableDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.w("[TimetableDB]onCancelled : $error")
                timetableDBCallback?.onValueCancelled(error)
            }

        })
    }

    private lateinit var memberCategoryDB: DatabaseReference
    private var memberCategoryDBCallback: IFirebaseCallback? = null

    fun registerMemberCategoryDB(callback: IFirebaseCallback) {
        if(!::memberCategoryDB.isInitialized) memberCategoryDB = userDB.child(FirebaseConstants.TABLE_MEMBER_CATEGORY)
        memberCategoryDBCallback = callback
        memberCategoryDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[MemberCategoryDB]onDataChange snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                if(Constants.IS_TEST_MODE) {
                    if (snapshot.childrenCount == 0L) {
                        val categoryList = generateTestClassInfo()
                        categoryList.forEachIndexed { index, item ->
                            val user = mutableMapOf<String, Any>()
                            user[FirebaseConstants.KEY_ID] = item.id!!
                            user[FirebaseConstants.KEY_TITLE] = item.title.orEmpty()
                            user[FirebaseConstants.KEY_VALUE] = item.value.orEmpty()
                            user[FirebaseConstants.KEY_IMAGE_URL] = item.imageUrl.orEmpty()
                            user[FirebaseConstants.KEY_UUID] = item.uuid
                            user[FirebaseConstants.KEY_TIMESTAMP] = item.timeStamp
                            memberCategoryDB.child("${FirebaseConstants.KEY_ITEM}$index")
                                .updateChildren(user)
                        }
                    }
                }
                //memberCategoryDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[MemberCategoryDB]onCancelled error : $error")
                memberCategoryDBCallback?.onValueCancelled(error)
            }

        })

        memberCategoryDB.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[MemberCategoryDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key?.isNotEmpty() == true) {
                    getMemberCategoryDataByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[MemberCategoryDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                //memberCategoryDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                memberCategoryDBCallback?.onEventChildChanged(snapshot, previousChildName)
                notifyAllDataChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[MemberCategoryDB]onChildRemoved snapshot : $snapshot")
                memberCategoryDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[MemberCategoryDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                memberCategoryDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[MemberCategoryDB]onCancelled error : $error")
                memberCategoryDBCallback?.onEventCancelled(error)
            }

        })
    }

    private fun getMemberCategoryDataByKey(key: String) {
        memberCategoryDB.child(key).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val entity = snapshot.getValue(SimpleEntity::class.java)
                Logger.d("[MemberCategoryDB]onDataChange entity : $entity")
                memberCategoryDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.w("[MemberCategoryDB]onCancelled : $error")
                memberCategoryDBCallback?.onValueCancelled(error)
            }

        })
    }

    private lateinit var memberInfoDB: DatabaseReference
    private var memberInfoDBCallback: IFirebaseCallback? = null

    fun registerMemberInfoDB(callback: IFirebaseCallback) {
        if(!::memberInfoDB.isInitialized) memberInfoDB = userDB.child(FirebaseConstants.TABLE_MEMBER_INFO)
        memberInfoDBCallback = callback
        memberInfoDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[MemberInfoDB]onDataChange snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                if(Constants.IS_TEST_MODE) {
                    if (snapshot.childrenCount == 0L) {
                        val memberInfoList = generateTestMemberInfoData()
                        memberInfoList.forEachIndexed { index, item ->
                            val user = mutableMapOf<String, Any>()
                            user[FirebaseConstants.KEY_ID] = item.id!!
                            user[FirebaseConstants.KEY_NAME] = item.name
                            user[FirebaseConstants.KEY_GENDER] = item.gender
                            user[FirebaseConstants.KEY_BIRTH] = item.birth
                            user[FirebaseConstants.KEY_TYPE] = item.type
                            user[FirebaseConstants.KEY_UUID] = item.uuid
                            user[FirebaseConstants.KEY_TIMESTAMP] = item.timeStamp
                            if (item.phoneNumber != null) user[FirebaseConstants.KEY_PHONE_NUMBER] =
                                item.phoneNumber!!
                            if (item.className != null) user[FirebaseConstants.KEY_CLASSNAME] =
                                item.className!!
                            if (item.imageUrl != null) user[FirebaseConstants.KEY_IMAGE_URL] =
                                item.imageUrl!!
                            if (item.detailInfo != null) user[FirebaseConstants.KEY_DETAIL_INFO] =
                                item.detailInfo!!
                            memberInfoDB.child("${FirebaseConstants.KEY_ITEM}$index")
                                .updateChildren(user)
                        }
                    }
                }
                //memberInfoDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[MemberInfoDB]onCancelled error : $error")
                memberInfoDBCallback?.onValueCancelled(error)
            }

        })

        memberInfoDB.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[MemberInfoDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key?.isNotEmpty() == true) {
                    getMemberInfoDataByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[MemberInfoDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                //memberInfoDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                val entity = snapshot.getValue(MemberInfoEntity::class.java)
                if(entity!=null) {
                    Thread(Runnable {
                        AppDatabase.getDatabase().memberInfoDao().insertData(entity)
                    }).start()
                }
                memberInfoDBCallback?.onEventChildChanged(snapshot, previousChildName)
                //notifyAllDataChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[MemberInfoDB]onChildRemoved snapshot : $snapshot")
                memberInfoDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[MemberInfoDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                memberInfoDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[MemberInfoDB]onCancelled error : $error")
                memberInfoDBCallback?.onEventCancelled(error)
            }

        })
    }

    private fun getMemberInfoDataByKey(key: String) {
        memberInfoDB.child(key).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val entity = snapshot.getValue(MemberInfoEntity::class.java)
                Logger.d("[MemberInfoDB]onDataChange entity : $entity")
                memberInfoDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.w("[MemberInfoDB]onCancelled : $error")
                memberInfoDBCallback?.onValueCancelled(error)
            }

        })
    }

    fun updateProfileImage(profileData: MemberInfoEntity?, imageUrl: String?) {
        if(profileData == null || imageUrl == null) return

        val user = mutableMapOf<String, Any>()
        user[FirebaseConstants.KEY_TIMESTAMP] = Util.getTimestamp()
        user[FirebaseConstants.KEY_IMAGE_URL] = imageUrl
        val keyId = profileData.id!!.toInt()-1
        memberInfoDB.child("${FirebaseConstants.KEY_ITEM}${keyId}").updateChildren(user)
    }

    private lateinit var calendarDB: DatabaseReference
    private var calendarDBCallback: IFirebaseCallback? = null

    fun registerCalendarDB(callback: IFirebaseCallback) {
        if(!::calendarDB.isInitialized) calendarDB = userDB.child(FirebaseConstants.TABLE_CALENDAR)
        calendarDBCallback = callback
        calendarDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[CalendarDB]onDataChange snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                if(Constants.IS_TEST_MODE) {
                    if (snapshot.childrenCount == 0L) {
                        val memberInfoList = generateTestCalendarData()
                        memberInfoList.forEachIndexed { index, item ->
                            val user = mutableMapOf<String, Any>()
                            user[FirebaseConstants.KEY_ID] = item.id!!
                            user[FirebaseConstants.KEY_TITLE] = item.title
                            if (item.detailInfo != null) user[FirebaseConstants.KEY_DETAIL_INFO] =
                                item.detailInfo!!
                            user[FirebaseConstants.KEY_DATE] = item.date
                            user[FirebaseConstants.KEY_SCHEDULE_TYPE] = item.scheduleType
                            user[FirebaseConstants.KEY_UUID] = item.uuid
                            user[FirebaseConstants.KEY_TIMESTAMP] = item.timeStamp
                            calendarDB.child("${FirebaseConstants.KEY_ITEM}$index")
                                .updateChildren(user)
                        }
                    }
                }
                //calendarDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[CalendarDB]onCancelled error : $error")
                calendarDBCallback?.onValueCancelled(error)
            }

        })

        calendarDB.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CalendarDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key?.isNotEmpty() == true) {
                    getCalendarDataByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CalendarDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                //calendarDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                calendarDBCallback?.onEventChildChanged(snapshot, previousChildName)
                notifyAllDataChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[CalendarDB]onChildRemoved snapshot : $snapshot")
                calendarDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[CalendarDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                calendarDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[CalendarDB]onCancelled error : $error")
                calendarDBCallback?.onEventCancelled(error)
            }

        })
    }

    private fun getCalendarDataByKey(key: String) {
        calendarDB.child(key).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val entity = snapshot.getValue(CalendarEntity::class.java)
                Logger.d("[CalendarDB]onDataChange entity : $entity")
                calendarDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.w("[CalendarDB]onCancelled : $error")
                calendarDBCallback?.onValueCancelled(error)
            }

        })
    }

    var classBoardDB: DatabaseReference? = null
    private var classBoardDBCallback: IFirebaseCallback? = null

    fun registerClassBoardDB(className: String, callback: IFirebaseCallback) {
        classBoardDB = userDB.child(FirebaseConstants.TABLE_CLASS_BOARD).child(className)
        classBoardDBCallback = callback
        classBoardDB?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[ClassBoardDB]onDataChange snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                /*
                if(snapshot.childrenCount == 0L) {
                    val boardList = generateTestBoardData()
                    boardList.forEachIndexed { index, item ->
                        val user = mutableMapOf<String, Any>()
                        user[FirebaseConstants.KEY_ID] = item.id!!
                        user[FirebaseConstants.KEY_TITLE] = item.title
                        user[FirebaseConstants.KEY_AUTHOR] = item.author
                        user[FirebaseConstants.KEY_DESCRIPTION] = item.description
                        user[FirebaseConstants.KEY_IMAGE_URL] = item.imageUrl.orEmpty()
                        user[FirebaseConstants.KEY_CLASSNAME] = item.className.orEmpty()
                        user[FirebaseConstants.KEY_UUID] = Util.getUUID()
                        user[FirebaseConstants.KEY_TIMESTAMP] = Util.getTimestamp()
                        classBoardDB.child("${FirebaseConstants.KEY_ITEM}$index").updateChildren(user)
                    }
                }
                 */
                //classBoardDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[ClassBoardDB]onCancelled error : $error")
                classBoardDBCallback?.onValueCancelled(error)
            }

        })

        classBoardDB?.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[ClassBoardDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key?.isNotEmpty() == true) {
                    getClassBoardDataByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[ClassBoardDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                //classBoardDB?.child(snapshot.key!!)?.child(FirebaseConstants.KEY_TIMESTAMP)?.setValue(Util.getTimestamp())
                classBoardDBCallback?.onEventChildChanged(snapshot, previousChildName)
                notifyAllDataChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[ClassBoardDB]onChildRemoved snapshot : $snapshot")
                classBoardDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[ClassBoardDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                classBoardDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[ClassBoardDB]onCancelled error : $error")
                classBoardDBCallback?.onEventCancelled(error)
            }

        })
    }

    private fun getClassBoardDataByKey(key: String) {
        classBoardDB?.child(key)?.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val entity = snapshot.getValue(BoardEntity::class.java)
                Logger.d("[ClassBoardDB]onDataChange entity : $entity")
                classBoardDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.w("[ClassBoardDB]onCancelled : $error")
                classBoardDBCallback?.onValueCancelled(error)
            }

        })
    }

    lateinit var freeBoardDB: DatabaseReference
    private var freeBoardDBCallback: IFirebaseCallback? = null

    fun registerFreeBoardDB(callback: IFirebaseCallback) {
        if(!::freeBoardDB.isInitialized) freeBoardDB = userDB.child(FirebaseConstants.TABLE_FREE_BOARD)
        freeBoardDBCallback = callback
        freeBoardDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Logger.d("[FreeBoardDB]onDataChange snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                /*
                if(snapshot.childrenCount == 0L) {
                    val freeBoardList = generateTestFreeBoardData()
                    freeBoardList.forEachIndexed { index, item ->
                        val user = mutableMapOf<String, Any>()
                        user[FirebaseConstants.KEY_ID] = item.id!!
                        user[FirebaseConstants.KEY_TITLE] = item.title
                        user[FirebaseConstants.KEY_AUTHOR] = item.author
                        user[FirebaseConstants.KEY_DESCRIPTION] = item.description
                        user[FirebaseConstants.KEY_IMAGE_URL] = item.imageUrl.orEmpty()
                        user[FirebaseConstants.KEY_UUID] = Util.getUUID()
                        user[FirebaseConstants.KEY_TIMESTAMP] = Util.getTimestamp()
                        freeBoardDB.child("${FirebaseConstants.KEY_ITEM}$index").updateChildren(user)
                    }
                }
                 */
                //freeBoardDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[FreeBoardDB]onCancelled error : $error")
                freeBoardDBCallback?.onValueCancelled(error)
            }

        })

        freeBoardDB.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[FreeBoardDB]onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key?.isNotEmpty() == true) {
                    getFreeBoardDataByKey(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[FreeBoardDB]onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                //freeBoardDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                freeBoardDBCallback?.onEventChildChanged(snapshot, previousChildName)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                Logger.d("[FreeBoardDB]onChildRemoved snapshot : $snapshot")
                freeBoardDBCallback?.onEventChildRemoved(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("[FreeBoardDB]onChildMoved snapshot : $snapshot / previousChildName : $previousChildName")
                freeBoardDBCallback?.onEventChildMoved(snapshot, previousChildName)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.d("[FreeBoardDB]onCancelled error : $error")
                freeBoardDBCallback?.onEventCancelled(error)
            }

        })
    }

    private fun getFreeBoardDataByKey(key: String) {
        freeBoardDB.child(key).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val entity = snapshot.getValue(BoardEntity::class.java)
                Logger.d("[FreeBoardDB]onDataChange entity : $entity")
                freeBoardDBCallback?.onValueDataChange(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Logger.w("[FreeBoardDB]onCancelled : $error")
                freeBoardDBCallback?.onValueCancelled(error)
            }

        })
    }


    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun getCurrentUserId(): String {
        if(getCurrentUser() == null) return FirebaseConstants.EMPTY_USER
        return getCurrentUser()?.uid.orEmpty()
    }

    private var notifyList: MutableList<IFirebaseNotify> = mutableListOf()
    fun registerNotify(notifyCallback: IFirebaseNotify) {
        if(!notifyList.contains(notifyCallback)) {
            notifyList.add(notifyCallback)
        }
        Logger.d("registerNotify size : ${notifyList.size}")
    }

    fun notifyAllDataChanged() {
        notifyList.forEach {
            it.onDataChanged()
        }
    }

    interface IFirebaseCallback {
        fun onValueDataChange(snapshot: DataSnapshot)
        fun onValueCancelled(error: DatabaseError)

        fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?)
        fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?)
        fun onEventChildRemoved(snapshot: DataSnapshot)
        fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?)
        fun onEventCancelled(error: DatabaseError)
    }

    interface IFirebaseNotify {
        fun onDataChanged()
    }
}