package test.kozachenkotest.Models

data class VenueItem(
    val name: String,
    val location: LocationItem,
    val stats: StatsItem,
    val url: String,
    val rating: Float,
    val hours: HoursItem?
)