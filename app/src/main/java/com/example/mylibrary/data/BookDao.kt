package com.example.mylibrary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    /** Books whose title or author contains [query], case-insensitively. An empty query returns every book. */
    @Query(
        """
        SELECT * FROM books
        WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE, id
        """
    )
    fun search(query: String): Flow<List<Book>>

    @Insert
    suspend fun insert(book: Book): Long

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)
}
