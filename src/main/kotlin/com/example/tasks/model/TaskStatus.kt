package com.example.tasks.model

enum class TaskStatus(val displayName: String) {
    TODO("待办"),
    IN_PROGRESS("进行中"),
    DONE("已完成");

    fun next(): TaskStatus = when (this) {
        TODO -> IN_PROGRESS
        IN_PROGRESS -> DONE
        DONE -> TODO
    }
}
