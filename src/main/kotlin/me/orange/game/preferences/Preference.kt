package me.orange.game.preferences

import kotlin.reflect.KClass

enum class Preference(type: KClass<*>) {
    MENUS_SIZE(Int::class),
    SHOW_COORDINATES(Boolean::class),
    MORE_ACTIONS(Boolean::class)
}
