package jp.gr.java_conf.stardiopside.ktlearn.sound.event

import org.springframework.context.ApplicationEvent

class SoundActionEvent(action: String) : ApplicationEvent(action) {
    val action: String
        get() = super.getSource() as String
}
