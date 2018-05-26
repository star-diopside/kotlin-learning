package jp.gr.java_conf.stardiopside.ktlearn.sound.event

import java.time.Duration

object SoundPositionFinishEvent : SoundPositionEvent(Duration.ZERO) {
    override val position: Duration?
        get() = null
}
