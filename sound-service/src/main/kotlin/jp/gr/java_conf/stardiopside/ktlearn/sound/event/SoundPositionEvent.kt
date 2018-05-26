package jp.gr.java_conf.stardiopside.ktlearn.sound.event

import org.springframework.context.ApplicationEvent
import java.time.Duration

open class SoundPositionEvent(position: Duration) : ApplicationEvent(position) {
    open val position: Duration?
        get() = super.getSource() as Duration
}
