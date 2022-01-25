package kr.co.skchurch.seokwangyouthdoor.ui.board.free

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.orhanobut.logger.Logger
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.adapter.FreeBoardListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.FreeBoardEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityFreeBoardBinding
import kr.co.skchurch.seokwangyouthdoor.ui.board.BoardViewModel
import kr.co.skchurch.seokwangyouthdoor.ui.board.detail.BoardDetailActivity
import kr.co.skchurch.seokwangyouthdoor.ui.widget.IDialogCallback
import kr.co.skchurch.seokwangyouthdoor.ui.widget.MessageDialog
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class FreeBoardActivity : AppCompatActivity() {

    companion object {
        private val TAG = FreeBoardActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityFreeBoardBinding
    private lateinit var adapter: FreeBoardListAdapter
    private lateinit var boardViewModel: BoardViewModel
    private var isCreated = false
    private lateinit var author: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFreeBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isCreated = true

        author = intent.getStringExtra(Constants.EXTRA_AUTHOR)!!

        initViews()
        bindViews()
    }

    private fun initViews() = with(binding) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        adapter = FreeBoardListAdapter(
            onItemClicked = { position, itemData ->
                Logger.d("boardList clicked : $position / $itemData")
            },
            onItemLongClicked = { position, itemData ->
                Logger.d("boardList long clicked : $position / $itemData")
                if(!Util.isOnline(this@FreeBoardActivity)) return@FreeBoardListAdapter
                if(itemData.author == author) {
                    showDeleteItemDialog(itemData)
                }
            },
            author)
        val recyclerLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerView.layoutManager = recyclerLayoutManager
        recyclerView.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE
        boardViewModel = ViewModelProvider(this@FreeBoardActivity).get(
            BoardViewModel::class.java)
        //boardViewModel.requestDataByClassName(currentClassName)

        boardViewModel.freeBoardListData.observe(this@FreeBoardActivity, Observer {
            Logger.d("freeBoardListData observe : $it")
            binding.progressBar.visibility = View.GONE
            adapter.submitList(it)
            if(it == null || it.isEmpty()) {
                emptyItem.visibility = View.VISIBLE
            }
            else {
                emptyItem.visibility = View.GONE
                Handler(mainLooper).postDelayed(Runnable {
                    recyclerView.smoothScrollToPosition(0)
                }, 100)
            }
        })
    }

    private fun bindViews() = with(binding) {
        addFloatingBtn.setOnClickListener {
            val intent = Intent(this@FreeBoardActivity, BoardDetailActivity::class.java)
            intent.putExtra(Constants.EXTRA_CLASS_NAME, Constants.FREE_BOARD_TITLE)
            intent.putExtra(Constants.EXTRA_AUTHOR, author)
            startActivity(intent)
        }

        refreshLayout.setOnRefreshListener {
            boardViewModel.requestCurrentFreeBoardData()
            Handler(mainLooper).postDelayed(Runnable {
                refreshLayout.isRefreshing = false
            }, 1000)
        }

        FirebaseManager.instance.registerNotify(object: FirebaseManager.IFirebaseNotify{
            override fun onDataChanged() {
                Logger.d("Notify onDataChanged")
                //binding.progressBar.visibility = View.VISIBLE
                boardViewModel.requestCurrentFreeBoardData()
            }
        })
    }

    private var deleteItemDialog: MessageDialog? = null
    private fun showDeleteItemDialog(targetData: FreeBoardEntity) {
        removeDeleteItemDialog()
        val btnData = arrayListOf<Pair<Int, String>>(
            Pair(0, getString(R.string.no)),
            Pair(1, getString(R.string.yes))
        )
        val listData = arrayListOf<SimpleEntity>(
            SimpleEntity(1L, getString(R.string.delete_this_board), null, null, Util.getUUID(), Util.getTimestamp())
        )
        deleteItemDialog = MessageDialog(
            getString(R.string.warning),
            listData,
            object: IDialogCallback {
                override fun dialogItemClicked(position: Int, data: Any?) {}

                override fun dialogBtnClicked(id: Int) {
                    when(id) {
                        1 -> {
                            Logger.d("Delete free board Event : $targetData")
                            boardViewModel.removeFreeBoard(targetData)
                            removeDeleteItemDialog()
                        }
                        else -> removeDeleteItemDialog()
                    }
                }

            },
            btnData)
        deleteItemDialog?.show(supportFragmentManager, "DeleteItemDialog")
    }

    private fun removeDeleteItemDialog() {
        deleteItemDialog?.dismiss()
        deleteItemDialog = null
    }

    override fun onResume() {
        super.onResume()
        Logger.d("onResume isCreated : $isCreated")
        binding.progressBar.visibility = View.VISIBLE
        //boardViewModel.requestDataByClassName(currentClassName)
        if(!isCreated) boardViewModel.requestCurrentFreeBoardData()
        isCreated = false
        if(!Util.isOnline(binding.root.context)) binding.addFloatingBtn.visibility = View.GONE
        else binding.addFloatingBtn.visibility = View.VISIBLE
    }
}