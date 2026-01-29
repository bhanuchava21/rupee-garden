package com.bhanu.rupeegarden.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

enum class SoundEffect {
    PLANT_SEED,
    PLANT_SPROUT,
    PLANT_YOUNG,
    PLANT_FULL,
    DAY_SAVED,
    DAY_SPENT,
    ACHIEVEMENT,
    LEVEL_UP,
    CONFETTI,
    BUTTON_TAP
}

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundEffect, Int>()
    private var isEnabled = true
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
        isInitialized = true
    }

    private fun loadSounds() {
        val pool = soundPool ?: return

        // Load each sound from res/raw
        soundIds[SoundEffect.PLANT_SEED] = loadSound(pool, "plant_seed")
        soundIds[SoundEffect.PLANT_SPROUT] = loadSound(pool, "plant_sprout")
        soundIds[SoundEffect.PLANT_YOUNG] = loadSound(pool, "plant_young")
        soundIds[SoundEffect.PLANT_FULL] = loadSound(pool, "plant_full")
        soundIds[SoundEffect.DAY_SAVED] = loadSound(pool, "day_saved")
        soundIds[SoundEffect.DAY_SPENT] = loadSound(pool, "day_spent")
        soundIds[SoundEffect.ACHIEVEMENT] = loadSound(pool, "achievement")
        soundIds[SoundEffect.LEVEL_UP] = loadSound(pool, "level_up")
        soundIds[SoundEffect.CONFETTI] = loadSound(pool, "confetti")
        soundIds[SoundEffect.BUTTON_TAP] = loadSound(pool, "button_tap")
    }

    private fun loadSound(pool: SoundPool, soundName: String): Int {
        val resourceId = context.resources.getIdentifier(
            soundName,
            "raw",
            context.packageName
        )
        return if (resourceId != 0) {
            pool.load(context, resourceId, 1)
        } else {
            0
        }
    }

    fun play(sound: SoundEffect, volume: Float = 1.0f) {
        if (!isEnabled || !isInitialized) return

        val soundId = soundIds[sound] ?: return
        if (soundId == 0) return

        soundPool?.play(
            soundId,
            volume.coerceIn(0f, 1f),
            volume.coerceIn(0f, 1f),
            1,
            0,
            1.0f
        )
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun isEnabled(): Boolean = isEnabled

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        isInitialized = false
    }
}
