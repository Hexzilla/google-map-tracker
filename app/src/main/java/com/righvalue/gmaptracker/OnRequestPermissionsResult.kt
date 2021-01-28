package com.righvalue.gmaptracker

interface OnRequestPermissionsResult {
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out kotlin.String>, grantResults: IntArray)
}