package kr.co.skchurch.seokwangyouthdoor.ui.more

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.orhanobut.logger.Logger
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityGuideBinding
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemGuideBinding

class GuideActivity : AppCompatActivity() {

    companion object {
        private val TAG = GuideActivity::class.java.simpleName
        private const val HANDLE_MSG_CHECK_LAST_PAGE = 10
        private val thumbnailData = arrayListOf<String>(
            "https://play-lh.googleusercontent.com/N7Wh2fVOF_bf4VqU28lzrkIqPUemGV0ZTVpMvuT1U0OQ3rI26gJY78XomON8FiRgqQ=w2560-h1440-rw",
            "https://play-lh.googleusercontent.com/AA-dKV3_vs7LkoZWLCzR9a13FsOkclhayNG8sBe4FLMym5RZ79680ojVvHJcB7rbjw=w2560-h1440-rw",
            "https://play-lh.googleusercontent.com/vLqdqcqXS1izYh6ESLGyP9gc_Y-v_FYdV7IgTQEAqVCJmCKUEUefayCVloGQ_pMpV1k=w2560-h1440-rw",
            "https://play-lh.googleusercontent.com/tHq8odCwsvUl2EVssQHTpL4u0N_eX31GJROynKDRglxXWe7HALgyy0uE1_cIbL5UK0Q=w2560-h1440-rw",
            "https://play-lh.googleusercontent.com/8m0uHMgNpGXQKnGrGoISVSZO_ETjzxDqsFM5pCT-VBEa4iit1BjNiXqvku1Jyp62BAO-=w2560-h1440-rw",
            "https://play-lh.googleusercontent.com/jhDNUCuj9_nZvLmfJcVXLwob6ooTvYegqH4syqPu4sj5ElVO-r5xuuf0p1QHz0A6VQ=w2560-h1440-rw",
            "https://play-lh.googleusercontent.com/GFfCCfZQSnqw6nJImNB1TeTDYDA6OtMewZL4APpQGjaqu5K3XIY4lYNBN5S-c8k9qCOC=w2560-h1440-rw"
        )
        private val infoStrData = arrayListOf<String>(
            "홈 탭에서 공지사항, 각종 이벤트 등 중요한 정보를 한눈에 볼 수 있습니다.",
            "예배 순서탭에서 이번주 예배 순서를 확인할 수 있습니다.",
            "소개 탭은 우리 청소년부 구성원들에 대해 소개하는 공간입니다.",
            "소개에서 각 반을 선택하면 해당 반의 소개 화면이 나옵니다. 우리반을 멋지게 꾸며 보세요!",
            "반별 소개에서 원하는 사람을 클릭하면 상세 소개 화면이 나옵니다. 서로를 더욱 알아가는 재미를 느껴보세요!",
            "더보기 탭에서 행사달력, 게시판, 주소록, 청소년부에 한마디!, 찬양배우기, 앱정보 등 다양한 기능을 활용 할 수 있습니다.",
            "행사달력은 청소년부의 각종 행사 및 구성원들의 생일 등을 날짜별로 확인할 수 있는 공간입니다.\n날짜 아래에 점 표시가 있는 날짜를 클릭하면 해당 날짜의 이벤트가 아래 리스트에 나옵니다."
        )
    }

    private lateinit var binding: ActivityGuideBinding

    private val handler by lazy {
        CustomHandler()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        bindViews()
    }

    private fun initViews() = with(binding) {
        var listData = mutableListOf<Pair<String, String>>()
        thumbnailData.forEachIndexed { index, thumbnail ->
            val info = infoStrData[index]
            listData.add(Pair(thumbnail, info))
        }

        viewPager.adapter = CustomAdapter(listData)
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun bindViews() = with(binding) {
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                showBottomButton(false)
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                if(state == ViewPager.SCROLL_STATE_IDLE) {
                    handler.removeMessages(HANDLE_MSG_CHECK_LAST_PAGE)
                    handler.sendEmptyMessageDelayed(HANDLE_MSG_CHECK_LAST_PAGE, 1000)
                }
            }

        })

        bottomBtn.setOnClickListener {
            finish()
        }
    }

    inner class CustomHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                HANDLE_MSG_CHECK_LAST_PAGE -> {
                    if(binding.viewPager.currentItem == binding.viewPager.adapter!!.count-1) showBottomButton(true)
                    //else showBottomButton(false)
                }
            }
            //super.handleMessage(msg)
        }
    }

    private fun showBottomButton(isShow: Boolean) {
        binding.bottomBtn.visibility = if(isShow) View.VISIBLE else View.GONE
    }

    inner class CustomAdapter(val listData: List<Pair<String, String>>): PagerAdapter() {

        override fun getCount(): Int = listData.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            Logger.d("instantiateItem position : $position")
            /*
            val inflater = (container.context.getSystemService(LAYOUT_INFLATER_SERVICE)) as LayoutInflater
            val rootView = inflater.inflate(R.layout.item_guide, container, false)
            val imageItemView = rootView.findViewById<ImageView>(R.id.item_image)
            Glide.with(container)
                .load(listData[position])
                .into(imageItemView)
            container.addView(rootView)
            return rootView
             */
            val itemBinding = ItemGuideBinding.inflate(LayoutInflater.from(container.context), container, false)
            val thumbnailUrl = listData[position].first
            val infoText = listData[position].second
            Glide.with(itemBinding.itemImage)
                .load(thumbnailUrl)
                .into(itemBinding.itemImage)
            container.addView(itemBinding.root)
            if(infoText.isNotEmpty()) {
                itemBinding.itemTxt.text = infoText
                itemBinding.itemTxt.visibility = View.VISIBLE
            }
            else itemBinding.itemTxt.visibility = View.GONE
            return itemBinding.root
            //return super.instantiateItem(container, position)
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            //super.destroyItem(container, position, `object`)
            (container as ViewPager).removeView(`object` as View)
        }

    }
}