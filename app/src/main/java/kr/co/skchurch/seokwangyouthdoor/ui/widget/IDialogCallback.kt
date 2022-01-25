package kr.co.skchurch.seokwangyouthdoor.ui.widget

interface IDialogCallback {
    fun dialogItemClicked(position: Int, data: Any?)
    fun dialogBtnClicked(id: Int)
}