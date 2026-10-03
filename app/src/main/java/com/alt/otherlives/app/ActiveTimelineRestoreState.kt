package com.alt.otherlives.app

data class ActiveTimelineRestoreState(
    val scenarioId: String?,
    val timelineKey: String?,
    val photoFileName: String?
) {
    companion object {
        fun restore(
            scenarioId: String?,
            timelineKey: String?,
            photoFileName: String?
        ): ActiveTimelineRestoreState = ActiveTimelineRestoreState(
            scenarioId = scenarioId.clean(),
            timelineKey = timelineKey.clean(),
            photoFileName = photoFileName.clean()
        )

        private fun String?.clean(): String? =
            this?.trim()?.takeIf { it.isNotEmpty() }
    }
}
