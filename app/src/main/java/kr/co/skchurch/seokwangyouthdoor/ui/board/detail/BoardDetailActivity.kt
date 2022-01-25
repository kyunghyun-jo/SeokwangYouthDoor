package kr.co.skchurch.seokwangyouthdoor.ui.board.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.orhanobut.logger.Logger
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.adapter.BoardDetailListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.BoardEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.FreeBoardEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityBoardDetailBinding
import kr.co.skchurch.seokwangyouthdoor.ui.board.BoardViewModel
import kr.co.skchurch.seokwangyouthdoor.utils.Util


class BoardDetailActivity : AppCompatActivity() {

    companion object {
        private val TAG = BoardDetailActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityBoardDetailBinding
    private lateinit var currentClassName: String
    private lateinit var adapter: BoardDetailListAdapter
    private lateinit var boardInfo: BoardEntity
    private var currentBoardMode = Constants.BOARD_MODE_VIEW
    private var mutableList: MutableList<HomeEntity> = arrayListOf()
    private lateinit var boardViewModel: BoardViewModel
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private var selectedImageUri: String? = null
    private lateinit var author: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedImageUri = null

        if(intent == null) {
            finish()
        }
        if(intent.getParcelableExtra<BoardEntity>(Constants.EXTRA_BOARD_INFO)!=null) {
            boardInfo = intent.getParcelableExtra<BoardEntity>(Constants.EXTRA_BOARD_INFO)!!
            Logger.d("onCreate boardInfo : $boardInfo")
        }
        else {
            currentClassName = intent.getStringExtra(Constants.EXTRA_CLASS_NAME)!!
            Logger.d("onCreate currentClassName : $currentClassName")
        }
        author = intent.getStringExtra(Constants.EXTRA_AUTHOR)!!

        initViews()
        bindViews()
    }

    private fun initViews() = with(binding) {
        currentBoardMode = if(::boardInfo.isInitialized) Constants.BOARD_MODE_VIEW else Constants.BOARD_MODE_ADD
        if(::currentClassName.isInitialized && currentClassName == Constants.FREE_BOARD_TITLE) currentBoardMode = Constants.FREE_BOARD_MODE_ADD

        boardViewModel = ViewModelProvider(this@BoardDetailActivity).get(
            BoardViewModel::class.java)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        adapter = BoardDetailListAdapter(onBtnClick = {
                when(it) {
                    BoardDetailListAdapter.BTN_ID_ATTACH -> {
                        pickFromGallery()
                    }
                    BoardDetailListAdapter.BTN_ID_DEATTACH -> {
                        deleteImage()
                    }
                    else -> {}
                }
            }
            //onEditListener = { entity, text -> }
        )
        recyclerView.layoutManager = LinearLayoutManager(this@BoardDetailActivity)
        recyclerView.adapter = adapter
        //adapter.submitList(list)
        refresh()

        when(currentBoardMode) {
            Constants.BOARD_MODE_VIEW -> {
                if(boardInfo.author == author) {
                    btnOk.setText(R.string.edit_board)
                    btnOk.visibility = View.VISIBLE
                    btnCancel.setText(R.string.cancel)
                }
                else {
                    btnOk.visibility = View.GONE
                    btnCancel.setText(R.string.ok)
                }
            }
            Constants.BOARD_MODE_ADD, Constants.FREE_BOARD_MODE_ADD -> {
                btnOk.setText(R.string.add_board)
                btnOk.visibility = View.VISIBLE
                btnCancel.setText(R.string.cancel)
            }
            else -> {
                btnOk.setText(R.string.edit_board)
                btnOk.visibility = View.VISIBLE
                btnCancel.setText(R.string.cancel)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if(Util.isOnline(this) &&
            ::boardInfo.isInitialized &&
            boardInfo.author == author) {
            menuInflater.inflate(R.menu.board_toolbar_menu, menu)
            true
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.tool_btn1 -> {
                showDeleteWarningDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteWarningDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.warning)
            .setMessage(R.string.delete_this_board)
            .setPositiveButton(R.string.yes) { dialog, which ->
                Logger.d("click yes button!")
                boardViewModel.removeBoard(boardInfo)
                finish()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .create()
            .show()
    }

    private fun bindViews() = with(binding) {
        btnOk.setOnClickListener {
            when(currentBoardMode) {
                Constants.BOARD_MODE_VIEW -> {
                    currentBoardMode = Constants.BOARD_MODE_EDIT
                    refresh()
                }
                Constants.BOARD_MODE_ADD -> {
                    Logger.d("click add button! selectedImageUri : $selectedImageUri")
                    //boardViewModel.addBoard(makeBoardEntity())
                    //finish()
                    if(selectedImageUri == null) {
                        boardViewModel.addBoard(makeBoardEntity())
                        finish()
                        return@setOnClickListener
                    }
                    progressBar.visibility = View.VISIBLE
                    val selectedImgUri = Uri.parse(selectedImageUri)
                    uploadPhoto(selectedImgUri,
                        successHandler = { storageUri ->
                            Logger.d("successHandler : $storageUri")
                            progressBar.visibility = View.GONE
                            boardViewModel.addBoard(makeBoardEntity(storageUri))
                            finish()
                        },
                        errorHandler = {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@BoardDetailActivity, "사진 업로드에 실패했습니다. ", Toast.LENGTH_SHORT).show()
                            boardViewModel.addBoard(makeBoardEntity())
                            finish()
                        })
                }
                Constants.BOARD_MODE_EDIT -> {
                    Logger.d("click edit button!")
                    //boardViewModel.editBoard(makeBoardEntity())
                    //finish()
                    if(selectedImageUri == null) {
                        boardViewModel.editBoard(makeBoardEntity())
                        finish()
                        return@setOnClickListener
                    }
                    progressBar.visibility = View.VISIBLE
                    val selectedImgUri = Uri.parse(selectedImageUri)
                    uploadPhoto(selectedImgUri,
                        successHandler = { storageUri ->
                            Logger.d("successHandler : $storageUri")
                            progressBar.visibility = View.GONE
                            boardViewModel.editBoard(makeBoardEntity(storageUri))
                            finish()
                        },
                        errorHandler = {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@BoardDetailActivity, "사진 업로드에 실패했습니다. ", Toast.LENGTH_SHORT).show()
                            boardViewModel.editBoard(makeBoardEntity())
                            finish()
                        })
                }
                Constants.FREE_BOARD_MODE_ADD -> {
                    Logger.d("click add free board button!")
                    boardViewModel.addFreeBoard(makeFreeBoardEntity())
                    //Handler(mainLooper).postDelayed(Runnable {
                    //    finish()
                    //}, 500)
                    finish()
                }
            }
        }
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun makeBoardEntity(storageUri: String? = null): BoardEntity {
        var boardEntity: BoardEntity
        if(currentBoardMode == Constants.BOARD_MODE_EDIT) {
            boardEntity = boardInfo.copy()
        }
        else {
            boardEntity = BoardEntity(
                null, "", author,
                "", currentClassName, null,
                Util.getUUID(), Util.getTimestamp())
        }
        adapter.currentList.forEach { homeEntity ->
            val imageUrl = if(!::boardInfo.isInitialized) storageUri
                            else {
                                storageUri ?: boardInfo.imageUrl
                            }
            when(homeEntity.type) {
                Constants.ITEM_TYPE_EDIT_SINGLE -> boardEntity.title = homeEntity.title!!
                Constants.ITEM_TYPE_EDIT_MULTI -> boardEntity.description = homeEntity.title!!
                Constants.ITEM_TYPE_ATTACH_IMAGE -> boardEntity.imageUrl = imageUrl
                else -> {}
            }
        }
        return boardEntity
    }

    private fun makeFreeBoardEntity(): FreeBoardEntity {
        val freeBoardEntity = FreeBoardEntity(
                null, "", author,
            "", null,
            Util.getUUID(), Util.getTimestamp())
        adapter.currentList.forEach { homeEntity ->
            when(homeEntity.type) {
                Constants.ITEM_TYPE_EDIT_SINGLE -> freeBoardEntity.title = homeEntity.title!!
                Constants.ITEM_TYPE_EDIT_MULTI -> freeBoardEntity.description = homeEntity.title!!
                else -> {}
            }
        }
        return freeBoardEntity
    }

    private val photoRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                Logger.d("photoRequest result : ${result.data}")
                val imageItem: HomeEntity? = mutableList.find { entity ->
                    entity.type == Constants.ITEM_TYPE_IMAGE ||
                            entity.type == Constants.ITEM_TYPE_ATTACH_IMAGE
                }
                imageItem?.imageUrl = result.data?.data.toString()
                selectedImageUri = imageItem?.imageUrl
                //adapter.submitList(mutableList.toList())
                adapter.notifyDataSetChanged()
            }
        }

    private fun pickFromGallery() {
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        //startActivityForResult(intent, PICK_IMAGE)
        photoRequestLauncher.launch(intent)
    }

    private fun deleteImage() {
        val imageItem: HomeEntity? = mutableList.find { entity ->
            entity.type == Constants.ITEM_TYPE_ATTACH_IMAGE
        }
        imageItem?.imageUrl = null
        adapter.notifyDataSetChanged()
    }

    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val storageKey = if(::currentClassName.isInitialized) "$currentClassName/image"
                        else "${boardInfo.className}/image"
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child(storageKey).child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    storage.reference.child(storageKey).child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                }
                else {
                    errorHandler()
                }
            }
    }

    private fun refresh() = with(binding) {
        //count = 0
        mutableList.clear()
        adapter.setBoardMode(currentBoardMode)
        when(currentBoardMode) {
            Constants.BOARD_MODE_VIEW -> {
                boardViewModel.requestDataByClassName(boardInfo.className!!)
                toolbar.title = getString(R.string.detail_board)
                btnOk.setText(R.string.edit)
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.title),
                null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_NORMAL, boardInfo.title,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.description),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_NORMAL, boardInfo.description,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                if(boardInfo.imageUrl!=null && boardInfo.imageUrl!!.isNotEmpty()) {
                    mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_IMAGE, null, null,
                        boardInfo.imageUrl, 0, Util.getUUID(), Util.getTimestamp()))
                }
            }
            Constants.BOARD_MODE_ADD -> {
                boardViewModel.requestDataByClassName(currentClassName)
                toolbar.title = getString(R.string.add_board)
                btnOk.setText(R.string.ok)
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.title),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_EDIT_SINGLE, null,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.description),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_EDIT_MULTI, null,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.attach_image),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_ATTACH_IMAGE, null,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
            }
            Constants.BOARD_MODE_EDIT -> {
                boardViewModel.requestDataByClassName(boardInfo.className!!)
                toolbar.title = getString(R.string.edit_board)
                btnOk.setText(R.string.ok)
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.title),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_EDIT_SINGLE, boardInfo.title,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.description),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_EDIT_MULTI, boardInfo.description,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.attach_image),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_ATTACH_IMAGE, null, null,
                    boardInfo.imageUrl, 0, Util.getUUID(), Util.getTimestamp()))
            }
            Constants.FREE_BOARD_MODE_ADD -> {
                //boardViewModel.refreshFreeBoard()
                toolbar.title = getString(R.string.add_board)
                btnOk.setText(R.string.ok)
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.title),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_EDIT_SINGLE, null,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_HEADER, getString(R.string.description),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                mutableList.add(HomeEntity(null, Constants.ITEM_TYPE_EDIT_MULTI, null,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
            }
        }
        adapter.submitList(mutableList.toList())
    }
}