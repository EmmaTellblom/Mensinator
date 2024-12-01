package com.mensinator.app.data

data class Symptom(
    val id: Int,
    val name: String,
    val active: Int, // Active: 1, Inactive: 0
    val color: String
)

val Symptom.isActive: Boolean
    get() = this.active == 1