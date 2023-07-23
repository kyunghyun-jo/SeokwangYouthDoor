package kr.co.skchurch.seokwangyouthdoor.data

import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.entities.*
import kr.co.skchurch.seokwangyouthdoor.utils.Util

object Constants {
    const val IS_TEST_MODE = true
    const val LOG_ENABLE = true

    const val GENDER_MAN = 0
    const val GENDER_WOMAN = 1

    const val MEMBER_TYPE_PASTER = 0
    const val MEMBER_TYPE_CHIEF_TEACHER = 1
    const val MEMBER_TYPE_WORSHIP_TEAM_LEADER = 2
    const val MEMBER_TYPE_TEACHER = 3
    const val MEMBER_TYPE_STUDENT = 4
    const val MEMBER_TYPE_STAFF = 5
    const val MEMBER_TYPE_UNKNOWN = 99

    const val SCHEDULE_TYPE_EVENT = 1
    const val SCHEDULE_TYPE_BIRTHDAY = 2

    const val BOARD_MODE_VIEW = 0
    const val BOARD_MODE_ADD = 1
    const val BOARD_MODE_EDIT = 2
    const val FREE_BOARD_MODE_ADD = 3

    const val ITEM_TYPE_HEADER = 1
    const val ITEM_TYPE_NORMAL = 0
    const val ITEM_TYPE_EDIT_SINGLE = 2
    const val ITEM_TYPE_EDIT_MULTI = 3
    const val ITEM_TYPE_ATTACH_IMAGE = 4
    const val ITEM_TYPE_IMAGE = 5
    const val ITEM_TYPE_NORMAL_MULTI = 6
    const val ITEM_TYPE_PROFILE_IMAGE = 7
    const val ITEM_TYPE_EMPTY = 99

    const val EXTRA_DETAIL_INFO = "detailInfo"
    const val EXTRA_TITLE = "title"
    const val EXTRA_CLASS_NAME = "className"
    const val EXTRA_BOARD_INFO = "boardInfo"
    const val EXTRA_MEMBER_INFO = "memberInfo"
    const val EXTRA_AUTHOR = "author"

    const val ACTION_CHANGE_VIEWPAGER_ENABLE = "kr.co.skchurch.seokwangyouthdoor.action.CHANGE_VIEWPAGER_ENABLE"
    const val EXTRA_IS_ENABLE = "isEnable"
    const val FREE_BOARD_TITLE = "_freeBoard_"
    const val NEW_MEMBER_VALUE = "_newMember_"

    const val OPTION_ALIGN_CENTER = "_alignCenter_"

    const val NETWORK_CHECK_DELAY = 1500L
    const val FIREBASE_CHECK_DELAY = 1000L
}

object FirebaseConstants {
    const val EMPTY_USER = "_empty_"
    const val TABLE_USERS = "Users"
    const val TABLE_HOME = "Home"
    const val TABLE_TIMETABLE = "Timetable"
    const val TABLE_MEMBER_INFO = "MemberInfo"
    const val TABLE_CALENDAR = "Calendar"
    const val TABLE_CLASS_BOARD = "ClassBoard"
    const val TABLE_FREE_BOARD = "FreeBoard"
    const val TABLE_COMMON_DATA = "CommonData"
    const val TABLE_MEMBER_CATEGORY = "MemberCategory"
    const val USER_ID = "userId"
    const val PREFIX_NOTICE = "NOTI"
    const val PREFIX_CHECK = "CHECK"
    const val PREFIX_OFFERING = "OFFERING"
    const val PREFIX_STAFF = "STAFF"
    const val PREFIX_EVENT = "_EVENT_"

    const val KEY_ITEM = "item"
    const val KEY_ID = "id"
    const val KEY_TITLE = "title"
    const val KEY_TYPE = "type"
    const val KEY_IMAGE_URL = "imageUrl"
    const val KEY_IMAGE_DRAWABLE_ID = "imageDrawableId"
    const val KEY_IS_NEW = "flagNew"
    const val KEY_LAST_VALUE = "lastValue"
    const val KEY_MIDDLE_VALUE = "middleValue"
    const val KEY_NAME = "name"
    const val KEY_GENDER = "gender"
    const val KEY_BIRTH = "birth"
    const val KEY_PHONE_NUMBER = "phoneNumber"
    const val KEY_CLASSNAME = "className"
    const val KEY_DETAIL_INFO = "detailInfo"
    const val KEY_DATE = "date"
    const val KEY_SCHEDULE_TYPE = "scheduleType"
    const val KEY_VALUE = "value"
    const val KEY_AUTHOR = "author"
    const val KEY_DESCRIPTION = "description"
    const val KEY_UUID = "uuid"
    const val KEY_TIMESTAMP = "timeStamp"

    const val KEY_WORSHIP_NOTICE = "worshipNotice"
    const val KEY_PRAISE_LINK = "praiseLink"

    const val TEST_EMAIL = "test@test.com"
    const val TEST_PASSWORD = "test01"
}

enum class MemberType(val id: Int, val value: String) {
    PASTER(Constants.MEMBER_TYPE_PASTER, SeokwangYouthApplication.context!!.getString(R.string.paster)),
    CHIEF_TEACHER(Constants.MEMBER_TYPE_CHIEF_TEACHER, SeokwangYouthApplication.context!!.getString(R.string.chief_teacher)),
    WORSHIP_TEAM_LEADER(Constants.MEMBER_TYPE_WORSHIP_TEAM_LEADER, SeokwangYouthApplication.context!!.getString(R.string.worship_team_leader)),
    TEACHER(Constants.MEMBER_TYPE_TEACHER, SeokwangYouthApplication.context!!.getString(R.string.teachers)),
    STUDENT(Constants.MEMBER_TYPE_STUDENT, SeokwangYouthApplication.context!!.getString(R.string.students)),
    STAFF(Constants.MEMBER_TYPE_STAFF, SeokwangYouthApplication.context!!.getString(R.string.staff)),
    UNKNOWN(Constants.MEMBER_TYPE_UNKNOWN, SeokwangYouthApplication.context!!.getString(R.string.unknown))
}

fun generateTestPraiseLink() = "https://youtube.com/playlist?list=PL_fjSppBJwhPcOPzshBJ_e_6JxlrjAZl5"
fun generateTestWorshipNotice() = "예배 시작 시간 : 9시 30분"

fun generateTestMemberInfoData(): List<MemberInfoEntity> {
    return mutableListOf(
        MemberInfoEntity(
            1, "심현준", Constants.GENDER_MAN, "1993.05.30", Constants.MEMBER_TYPE_PASTER,
            "010-1111-2222", null, "https://picsum.photos/200",
            MemberDetailInfoEntity("master@aaa.aaa", null, "신학대학원", "aaa@aaa.com", "영화보기", true),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            2, "김현진", Constants.GENDER_MAN, "1973.10.11", Constants.MEMBER_TYPE_CHIEF_TEACHER,
            "010-3333-4444", null, "https://picsum.photos/200",
            MemberDetailInfoEntity("aaa@aaa.aaa", null, "금융업", "bbb@bbb.com", "등산", false),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            3, "조경현", Constants.GENDER_MAN, "1983.03.08", Constants.MEMBER_TYPE_WORSHIP_TEAM_LEADER,
            "010-5555-6666", "찬양팀", "https://picsum.photos/200",
            MemberDetailInfoEntity("club0272@gmail.com", null, "프로그래머", "ccc@ccc.com", "기타연주", false),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            4, "민영미", Constants.GENDER_WOMAN, "1975.07.05", Constants.MEMBER_TYPE_TEACHER,
            "010-7777-8888", "사랑", "https://picsum.photos/200",
            MemberDetailInfoEntity("ccc@ccc.ccc", 55, "교육업", "ddd@ddd.com", "독서", true),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            5, "윤다정", Constants.GENDER_WOMAN, "1988.08.01", Constants.MEMBER_TYPE_TEACHER,
            "010-9999-0000", "라임", "https://picsum.photos/200",
            MemberDetailInfoEntity("ddd@ddd.ddd", 28, "회사원", "eee@eee.com", "음악감상", true),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            6, "이서연", Constants.GENDER_WOMAN, "1997.02.15", Constants.MEMBER_TYPE_TEACHER,
            "010-1212-3434", "GOT,찬양팀", "https://picsum.photos/200",
            MemberDetailInfoEntity("bbb@bbb.bbb", 21, "대학생", "fff@fff.com", "드럼연주", true),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            7, "김태린", Constants.GENDER_WOMAN, "2005.11.14", Constants.MEMBER_TYPE_STUDENT,
            "010-9136-7191", "사랑", "https://picsum.photos/200",
            MemberDetailInfoEntity("cassia1114@naver.com", 18, "용호고", null, "산책하기", false),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            8, "박동혁", Constants.GENDER_MAN, "2005.03.24", Constants.MEMBER_TYPE_STUDENT,
            "010-9274-7869", "사랑", "https://picsum.photos/200",
            MemberDetailInfoEntity("dan050324@naver.com", 19, "관양고", null, "웹툰보기, 축구", false),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            9, "김정제", Constants.GENDER_MAN, "2004.03.06", Constants.MEMBER_TYPE_STUDENT,
            "010-5338-2247", "사랑,찬양팀", "https://picsum.photos/200",
            MemberDetailInfoEntity("rlawjdwp04@naver.com", 19, "양명고", null, "농구게임", true),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            10, "박상훈", Constants.GENDER_MAN, "2007.11.11", Constants.MEMBER_TYPE_STUDENT,
            "010-5346-7589", "사랑", "https://picsum.photos/200",
            MemberDetailInfoEntity("jjj@jjj.com", 15, "당정중", "jjj@jjj.com", "농구", false),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            11, "서민형", Constants.GENDER_WOMAN, "2007.08.10", Constants.MEMBER_TYPE_STUDENT,
            "010-8754-2644", "라임,찬양팀", "https://picsum.photos/200",
            MemberDetailInfoEntity("kkk@kkk.com", 16, "당정중", "kkk@kkk.com", "배틀그라운드", false),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            12, "신서윤", Constants.GENDER_WOMAN, "2008.02.22", Constants.MEMBER_TYPE_STUDENT,
            "010-3546-0548", "GOT", "https://picsum.photos/200",
            MemberDetailInfoEntity("lll@lll.com", 14, "당정중", "lll@lll.com", "여행가기", false),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            13, "새신자1", Constants.GENDER_WOMAN, "2006.10.15", Constants.MEMBER_TYPE_STUDENT,
            "010-2828-9797", "새신자", "https://picsum.photos/200",
            MemberDetailInfoEntity("mmm@mmm.com", 14, "경기외고", "mmm@mmm.com", "야구관람", false),
            Util.getUUID(), Util.getTimestamp()
        ),
        MemberInfoEntity(
            14, "새신자2", Constants.GENDER_MAN, "2007.04.19", Constants.MEMBER_TYPE_STUDENT,
            null, "새신자", "https://picsum.photos/200",
            MemberDetailInfoEntity(null, 14, "당정중", null, "멍때리기", false),
            Util.getUUID(), Util.getTimestamp()
        ),
    )
}

fun generateTestHomeData(): List<HomeEntity> {
    return mutableListOf<HomeEntity>(
        HomeEntity(1, Constants.ITEM_TYPE_NORMAL, "NOTI_수능 기도회 : 11월1~10일",
                "여러분도 언젠가 고3이 됩니다. 중1부터 고3까지 6년동안의 세월을 견디고 결실을 맻는 선배들을 위해 기도합니다.", null, 1,
            Util.getUUID(), Util.getTimestamp()),
        HomeEntity(2, Constants.ITEM_TYPE_NORMAL, "NOTI_전도사님 심방 : GOT반",
                "먹고싶은 메뉴 생각해 놓으세요~! 만원의 행복 갑니다~!", null, 1,
            Util.getUUID(), Util.getTimestamp()),
        HomeEntity(3, Constants.ITEM_TYPE_NORMAL, "CHECK_GOT반 : 5명", null, null, 0,
            Util.getUUID(), Util.getTimestamp()),
        HomeEntity(4, Constants.ITEM_TYPE_NORMAL, "CHECK_라임반 : 3명", null, null, 0,
            Util.getUUID(), Util.getTimestamp()),
        HomeEntity(5, Constants.ITEM_TYPE_NORMAL, "CHECK_사랑반 : 4명", null, null, 0,
            Util.getUUID(), Util.getTimestamp())
    )
}

fun generateTestClassInfo(): List<SimpleEntity> {
    val paster = SeokwangYouthApplication.context?.getString(R.string.paster)
    val teachers = SeokwangYouthApplication.context?.getString(R.string.teachers)
    val new_member = SeokwangYouthApplication.context?.getString(R.string.new_member)
    val worship_team = SeokwangYouthApplication.context?.getString(R.string.worship_team)
    return mutableListOf<SimpleEntity>(
        SimpleEntity(1, paster, null, null, Util.getUUID(), Util.getTimestamp()),
        SimpleEntity(2, teachers,
            "은 아이들의 영적 성장을 최우선으로 하는 신앙 선배들 입니다.",
            "https://image.shutterstock.com/image-vector/teacher-school-boy-vector-illustration-600w-477275215.jpg",
            Util.getUUID(), Util.getTimestamp()
        ),
        SimpleEntity(3, "GOT",
            "은 모든 아이들이 함께 하나님의 사랑을 노래하는 즐거운 반입니다.",
            "https://image.shutterstock.com/image-photo/group-friends-standing-by-car-600w-275521547.jpg",
            Util.getUUID(), Util.getTimestamp()
        ),
        SimpleEntity(4, "라임",
            "은 모든 아이들이 함께 하나님의 사랑을 노래하는 즐거운 반입니다.",
            "https://image.shutterstock.com/image-photo/group-friends-enjoying-party-throwing-600w-565003417.jpg",
            Util.getUUID(), Util.getTimestamp()
        ),
        SimpleEntity(5, "사랑",
            "은 모든 아이들이 함께 하나님의 사랑을 노래하는 즐거운 반입니다.",
            "https://image.shutterstock.com/image-photo/group-happy-friends-having-breakfast-600w-1201677928.jpg",
            Util.getUUID(), Util.getTimestamp()
        ),
        SimpleEntity(6, new_member,
            "은 천하보다 귀한 한 영혼이 적응하도록 돕는 반입니다.",
            "https://image.shutterstock.com/image-vector/adult-guys-men-two-best-600w-583822957.jpg",
            Util.getUUID(), Util.getTimestamp()
        ),
        SimpleEntity(7, worship_team,
            "은 각자의 달란트로 기쁘게 하나님께 영광돌리는 사람들입니다.",
            "https://image.shutterstock.com/image-photo/rock-music-band-performing-female-600w-1938848485.jpg",
            Util.getUUID(), Util.getTimestamp()
        )
    )
}

private var timetableIdCount = 0L
fun generateTestTimetableData(): List<TimetableEntity> {
    return mutableListOf<TimetableEntity>(
        TimetableEntity(++timetableIdCount, "사도신경", null, "다같이",
            Util.getUUID(), Util.getTimestamp()),
        TimetableEntity(++timetableIdCount, "찬양", "하나님의 사랑이 외 3곡", "찬양팀",
            Util.getUUID(), Util.getTimestamp()),
        TimetableEntity(++timetableIdCount, "성경봉독", "출애굽기\n3장 1~4절", "심현준\n전도사님",
            Util.getUUID(), Util.getTimestamp()),
        TimetableEntity(++timetableIdCount, "설교말씀", "말씀으로 모세를\n부르신 하나님", "심현준\n전도사님",
            Util.getUUID(), Util.getTimestamp()),
        TimetableEntity(++timetableIdCount, "기도", null, "심현준\n전도사님",
            Util.getUUID(), Util.getTimestamp()),
        TimetableEntity(++timetableIdCount, "헌금", "헌금 찬양\n예배자", "GOT반",
            Util.getUUID(), Util.getTimestamp()),
        TimetableEntity(++timetableIdCount, "광고", null, "심현준\n전도사님",
            Util.getUUID(), Util.getTimestamp()),
        TimetableEntity(++timetableIdCount, "주기도문", null, "다같이",
            Util.getUUID(), Util.getTimestamp()),
        TimetableEntity(++timetableIdCount, "공과공부", "말씀 나눔", "반별로",
            Util.getUUID(), Util.getTimestamp())
    )
}

private var eventCount = 0L
fun generateTestCalendarData(): List<CalendarEntity> {
    return mutableListOf(
        CalendarEntity(++eventCount, "신년감사주일", null, "2022.1.2", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "겨울 수련회", "코로나로 인한 온라인 수련회", "2022.1.9", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "청소년부 제자 훈련", "담당 : 전도사님", "2022.1.16", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "교사 단합", "장소 : 미정", "2022.1.30", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "찬양팀 교육(4주)", "담당 : 전도사님", "2022.2.6", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "청소년부 겨울 수련회", "코로나로 인한 온라인 수련회", "2022.2.13", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "찬양팀 단합 워크숍", "장소 : 미정", "2022.2.27", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "생일파티(1~3월)", "축하합니다!", "2022.3.6", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "리더 훈련 시작", "담당 : 전도사님", "2022.3.6", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "학습, 세례", "반별 대상자", "2022.3.20", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "종려주일/고난주일", "경건한 사순절 지키기", "2022.3.27", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "반 시상(1월~3월)", "시상의 주인공은?", "2022.3.27", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "부활절", "부활절 감사 예배", "2022.4.3", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "중간고사 기간", "결석 방지 특별 관리", "2022.4.17", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "중간고사 기간", "결석 방지 특별 관리", "2022.4.24", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "어린이 주일", "청소년 주일", "2022.5.1", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "어버이 주일", "부모님과 함께 하는 예배", "2022.5.8", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "교사 주일", "교사 감사 예배", "2022.5.15", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "시험 스트레스 부시기 프로젝트", "무엇을 할까요?", "2022.5.29", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "생일파티(4~6월)", "축하합니다!", "2022.6.5", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "반 시상(4~6월)", "시상의 주인공은?", "2022.6.26", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "맥추감사주일", null, "2022.7.3", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "기말고사 기간", "결석 방지 특별 관리", "2022.7.3", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "기말고사 기간", "결석 방지 특별 관리", "2022.7.10", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "찬양팀 단합 워크숍", "장소 : 미정", "2022.7.24", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "여름 수련회", "코로나로 인한 온라인 수련회", "2022.8.14", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "생일파티(7~9월)", "축하합니다!", "2022.9.4", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "반 시상(7~9월)", "시상의 주인공은?", "2022.9.25", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "중간고사 기간", "결석 방지 특별 관리", "2022.9.25", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "학습, 세례", "반별 대상자", "2022.10.2", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "중간고사 기간", "결석 방지 특별 관리", "2022.10.2", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "수능 기도회", "수능생 화이팅!", "2022.10.23", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "리더 제자 훈련 엠티", "장소 : 미정", "2022.10.30", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "수험생 위로, 축복, 수능기도회", null, "2022.11.13", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "추수감사주일", "추수감사예배", "2022.11.20", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "기말고사 기간", "결석 방지 특별 관리", "2022.11.27", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "생일파티(10~12월)", "축하합니다!", "2022.12.4", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "기말고사 기간", "결석 방지 특별 관리", "2022.12.4", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "교사 워크숍", "장소 : 미정", "2022.12.18", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "성탄절", "성탄 축하 예배", "2022.12.25", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "연말 시상", "반 시상(10~12월) / 전체 시상 / 개인 시상", "2022.12.25", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
        CalendarEntity(++eventCount, "송구 영신 예배", null, "2022.12.25", Constants.SCHEDULE_TYPE_EVENT,
            Util.getUUID(), Util.getTimestamp()),
    )
}

fun generateTestBoardData(): List<BoardEntity> {
    return mutableListOf(
        BoardEntity(1, "우와~ 게시판이다!", "aaa@aaa.aaa",
            "자세한 내용", "GOT", "https://picsum.photos/200", Util.getUUID(), Util.getTimestamp()),
        BoardEntity(2, "이거 잘써봅시다~!", "bbb@bbb.bbb",
            "자세한 내용2", "GOT", "https://picsum.photos/200", Util.getUUID(), Util.getTimestamp())
    )
}

fun generateTestFreeBoardData(): List<FreeBoardEntity> {
    return mutableListOf(
        FreeBoardEntity(1, "제목1", "aaa@aaa.aaa",
            "최근 남아프리카공화국 등에서 확인된 신종 코로나바이러스 감염증(코로나19) " +
                    "새 변이(B.1.1.529)가 최악의 변종이 될 수 있다는 경고가 나오며 각국이 긴장하고 있다. " +
                    "국내에선 아직 확인된 사례가 없지만, 보건 당국은 출국 전 유전자 증폭(PCR) 검사에서 " +
                    "음성이 확인된 해외 입국자를 전수 검사해 변이 여부를 모니터링하겠다고 밝혔다.", "https://picsum.photos/200",
            Util.getUUID(), Util.getTimestamp()),
        FreeBoardEntity(2, "제목2", "bbb@bbb.bbb",
            "26일 김은진 중앙방역대책본부 검사분석팀장은 브리핑에서 새 변이와 관련, " +
                    "“누 변이라고 불리는 ‘B.1.1.529’는 아직 정확히 명명되지 않았다”라면서 " +
                    "“WHO(세계보건기구) 전문가 회의를 통해 관심 변이(VOI) 또는 주요 변이(VOC)로 " +
                    "결정되면 명명될 것”이라고 전했다.", "https://picsum.photos/200",
            Util.getUUID(), Util.getTimestamp()),
        FreeBoardEntity(3, "제목3", "ccc@ccc.ccc",
            "파이트 클럽 재밌다.", "https://picsum.photos/200",
            Util.getUUID(), Util.getTimestamp()),
        FreeBoardEntity(4, "제목4", "ddd@ddd.ddd",
            "당국에 따르면 WHO 데이터베이스(DB)에 등록된 새 변이 확진자는 현재까지 " +
                    "남아공, 보츠와나, 홍콩 등에서 66건이다. 그러나 BBC 보도에 따르면 남아공 가우텡주에서만 " +
                    "77건이 확인됐다고 한다.", "https://picsum.photos/200",
            Util.getUUID(), Util.getTimestamp()),
        FreeBoardEntity(5, "제목5", "eee@eee.eee",
            "피의 게임은 재밌을까?", "https://picsum.photos/200",
            Util.getUUID(), Util.getTimestamp())
    )
}