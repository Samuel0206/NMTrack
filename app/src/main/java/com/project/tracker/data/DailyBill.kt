package com.project.tracker.data

data class DailyBill(
    val id: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val type: String = "",
    val usage: String = ""
)