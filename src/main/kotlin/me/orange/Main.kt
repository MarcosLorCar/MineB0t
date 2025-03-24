package me.orange

fun saveAll() {
    println("Saving all chunks before shutdown...")
    GamesManager.saveAll()
    println("All chunks saved!")
}

fun main() {
    // Add shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        saveAll()
    })

    MineB0t.start()
}