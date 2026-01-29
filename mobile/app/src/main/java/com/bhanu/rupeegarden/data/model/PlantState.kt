package com.bhanu.rupeegarden.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlantState(val displayName: String) {
    SEED("Seed"),
    SPROUT("Sprout"),
    YOUNG("Young Plant"),
    FULL("Full Tree"),
    WITHERED("Withered")
}
