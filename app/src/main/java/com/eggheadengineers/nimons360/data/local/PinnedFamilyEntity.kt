package com.eggheadengineers.nimons360.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_families")
data class PinnedFamilyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconUrl: String,
)
