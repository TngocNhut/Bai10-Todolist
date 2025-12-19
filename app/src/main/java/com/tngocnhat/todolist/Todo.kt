package com.tngocnhat.todolist

data class Todo(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)
