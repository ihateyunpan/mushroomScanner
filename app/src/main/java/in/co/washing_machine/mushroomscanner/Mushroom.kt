package `in`.co.washing_machine.mushroomscanner

data class Mushroom(
    val id: String,
    val name: String,
    val starter: String,
    val wood: String? = null,      // 可空，默认 null
    val light: String? = null,     // 可空，默认 null
    val humidifier: String? = null,// 可空，默认 null
    val time: String? = null,      // 可空，默认 null
    val special: String? = null,   // 可空，默认 null
    val save: Boolean = false      // 默认为 false
)