package kr.co.skchurch.seokwangyouthdoor.ui.board

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.orhanobut.logger.Logger
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.adapter.BoardListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityBoardBinding
import kr.co.skchurch.seokwangyouthdoor.ui.board.detail.BoardDetailActivity
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class BoardActivity : AppCompatActivity() {

    companion object {
        private val TAG = BoardActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityBoardBinding
    private lateinit var currentClassName: String
    private lateinit var adapter: BoardListAdapter
    private lateinit var boardViewModel: BoardViewModel
    private lateinit var author: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent == null || intent.getStringExtra(Constants.EXTRA_CLASS_NAME) == null) {
            finish()
        }
        currentClassName = intent.getStringExtra(Constants.EXTRA_CLASS_NAME)!!
        Logger.d("onCreate currentClassName : $currentClassName")

        SeokwangYouthApplication.getMyProfile {
            author = it?.name ?: FirebaseManager.instance.getCurrentUser()?.email.orEmpty()
        }

        initViews()
        bindViews()
    }

    private fun initViews() = with(binding) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        collapsingToolbar.title = currentClassName + " " + getString(R.string.board)

        Thread(Runnable {
            val targetClassName = if(currentClassName == getString(R.string.worship_team)) currentClassName
                                    else currentClassName + getString(R.string.className)
            if(AppDatabase.getDatabase().memberCategoryDao().getDataByTitle(targetClassName).isEmpty()) return@Runnable
            val currentCategoryInfo = AppDatabase.getDatabase().memberCategoryDao().getDataByTitle(targetClassName)[0]
            Logger.d("initViews currentCategoryInfo : $currentCategoryInfo")
            if(currentCategoryInfo != null) {
                Handler(mainLooper).post(Runnable {
                    Glide.with(topInfoImage)
                        .load(currentCategoryInfo.imageUrl)
                        .apply(RequestOptions.centerCropTransform())
                        .into(topInfoImage)
                })
            }
        }).start()

        adapter = BoardListAdapter(onItemClicked = { position, itemData ->
            Logger.d("boardList clicked : $position / $itemData")
            val intent = Intent(this@BoardActivity, BoardDetailActivity::class.java)
            intent.putExtra(Constants.EXTRA_BOARD_INFO, itemData)
            intent.putExtra(Constants.EXTRA_AUTHOR, author)
            startActivity(intent)
        })
        recyclerView.layoutManager = LinearLayoutManager(this@BoardActivity)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(this@BoardActivity, LinearLayoutManager.VERTICAL))

        binding.progressBar.visibility = View.VISIBLE
        boardViewModel = ViewModelProvider(this@BoardActivity).get(
            BoardViewModel::class.java)
        //boardViewModel.requestDataByClassName(currentClassName)

        boardViewModel.boardListData.observe(this@BoardActivity, Observer { list ->
            Logger.d("listdata observe : $list")
            binding.progressBar.visibility = View.GONE
            adapter.submitList(list.sortedByDescending { it.timeStamp })
            if(list == null || list.isEmpty()) {
                emptyItem.visibility = View.VISIBLE
            }
            else emptyItem.visibility = View.GONE
        })
    }

    private fun bindViews() = with(binding) {
        addFloatingBtn.setOnClickListener {
            val intent = Intent(this@BoardActivity, BoardDetailActivity::class.java)
            intent.putExtra(Constants.EXTRA_CLASS_NAME, currentClassName)
            intent.putExtra(Constants.EXTRA_AUTHOR, author)
            startActivity(intent)
        }

        refreshLayout.setOnRefreshListener {
            boardViewModel.requestDataByClassName(currentClassName)
            Handler(mainLooper).postDelayed(Runnable {
                refreshLayout.isRefreshing = false
            }, 1000)
        }

        FirebaseManager.instance.registerNotify(object: FirebaseManager.IFirebaseNotify{
            override fun onDataChanged() {
                Logger.d("Notify onDataChanged")
                //binding.progressBar.visibility = View.VISIBLE
                boardViewModel.requestDataByClassName(currentClassName)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.VISIBLE
        boardViewModel.requestDataByClassName(currentClassName)
        if(!Util.isOnline(binding.root.context)) binding.addFloatingBtn.visibility = View.GONE
        else binding.addFloatingBtn.visibility = View.VISIBLE
    }
}