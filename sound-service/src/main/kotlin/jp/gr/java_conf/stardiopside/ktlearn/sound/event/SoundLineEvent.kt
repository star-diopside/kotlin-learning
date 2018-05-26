package jp.gr.java_conf.stardiopside.ktlearn.sound.event

import org.springframework.context.ApplicationEvent
import javax.sound.sampled.LineEvent

class SoundLineEvent(event: LineEvent) : ApplicationEvent(event) {
    val lineEvent: LineEvent
        get() = super.getSource() as LineEvent
}
