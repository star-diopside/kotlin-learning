package jp.gr.java_conf.stardiopside.ktlearn.sound.event

import org.springframework.context.ApplicationEvent

class SoundExceptionEvent(exception: Exception) : ApplicationEvent(exception) {
    val exception: Exception
        get() = super.getSource() as Exception
}
