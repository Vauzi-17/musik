package com.lyraplayer.data

data class LyricLine(
    val time: Long,
    val text: String
)

object LrcParser {

    fun parse(lrcText: String): List<LyricLine> {
        val lines = lrcText.lines()
        val result = mutableListOf<LyricLine>()

        val regex = Regex("""\[(\d+):(\d+).(\d+)](.*)""")

        for (line in lines) {
            val match = regex.find(line)
            if (match != null) {
                val minutes = match.groupValues[1].toLong()
                val seconds = match.groupValues[2].toLong()
                val millis = match.groupValues[3].toLong()
                val text = match.groupValues[4]

                val timeInMillis = minutes * 60000 + seconds * 1000 + millis

                result.add(LyricLine(timeInMillis, text))
            }
        }

        return result.sortedBy { it.time }
    }
}
