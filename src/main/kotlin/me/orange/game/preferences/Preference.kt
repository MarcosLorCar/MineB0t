package me.orange.game.preferences

import kotlin.reflect.KClass

enum class Preference(
    val valueType: KClass<*>,
    val default: Any,
    val options: List<Any>
) {
    MENUS_SIZE(Int::class, 10, listOf(10, 15, 20)),
    SHOW_COORDINATES(Boolean::class, false, listOf(true, false)),
    MORE_ACTIONS(Boolean::class, false, listOf(true, false)),
    HEAD_EMOJI(String::class, "😀", listOf("😀", "😎", "🤠"));
}