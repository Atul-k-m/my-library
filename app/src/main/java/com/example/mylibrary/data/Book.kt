package com.example.mylibrary.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate

enum class ReadingStatus(val label: String) {
    TO_READ("To read"),
    READING("Reading"),
    READ("Read"),
}

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String = "",
    val status: ReadingStatus = ReadingStatus.TO_READ,
    /** Who currently has the book; null while it's on the shelf. */
    val lentTo: String? = null,
    val lentOn: LocalDate? = null,
) {
    val isLent: Boolean get() = lentTo != null
}

class Converters {
    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()
}
