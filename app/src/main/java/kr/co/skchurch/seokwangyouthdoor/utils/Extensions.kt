package kr.co.skchurch.seokwangyouthdoor.utils

fun String.getBirthDate(): String {
    var tempArr = this.split(".").toMutableList()
    if(tempArr[1].length == 1) tempArr[1] = "0"+tempArr[1]
    if(tempArr[2].length == 1) tempArr[2] = "0"+tempArr[2]
    return tempArr[1]+"."+tempArr[2]
}