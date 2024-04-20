package com.tambapps.marcel.android.marshell.ui.model

data class Prompt(val input: String?, val result: Result?) {
    data class Result(
        val output: String,
        val success: Boolean
    )
}
