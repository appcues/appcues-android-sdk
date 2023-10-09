package com.appcues.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.appcues.data.local.model.ExperienceRulesEntity
import java.util.UUID

@Dao
internal interface ExperienceRulesDao {

    /**
     * Insert element into table
     */
    @Insert
    fun insert(rulesEntity: ExperienceRulesEntity)

    /**
     * retrieve last element by id where experienceId matches
     *
     * can return element or null
     */
    @Query("SELECT * FROM ExperienceRulesEntity WHERE experienceId == :experienceId ORDER BY id DESC LIMIT 1")
    suspend fun getRules(experienceId: UUID): ExperienceRulesEntity?

    /**
     * count entries for that experienceId seen by that user id and return count
     */
    @Query("SELECT COUNT(*) FROM ExperienceRulesEntity WHERE experienceId == :experienceId AND userId == :userId")
    suspend fun getViewCount(userId: String, experienceId: UUID): Int
}
