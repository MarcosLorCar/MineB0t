package me.orange.game.preferences

import kotlin.reflect.KClass

enum class Preference(
    val valueType: KClass<*>,
    val default: Any,
    val options: List<Any>
) {
    MENUS_SIZE(Int::class, 10, listOf(5, 10, 20)),
    SHOW_COORDINATES(Boolean::class, true, listOf(true, false)),
    UI_MODE(String::class, "minimal", listOf("minimal", "hotbar", "extended", "extended_hotbar")),
    PLAYER_TIMEOUT(Int::class, 30, listOf(30, 60, 90)),
    ITEM_PICKUP_FEEDBACK(Boolean::class, true, listOf(true, false)),
    HEAD_EMOJI(String::class, "😀", listOf("😀", "🥸", "🧐", "🤓", "😎", "😡", "🥶", "🤢", "😈", "🤡", "👽", "😬", "😼", "😺"));
}