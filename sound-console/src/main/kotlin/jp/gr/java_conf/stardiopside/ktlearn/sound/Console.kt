package jp.gr.java_conf.stardiopside.ktlearn.sound

import jp.gr.java_conf.stardiopside.ktlearn.sound.event.SoundActionEvent
import jp.gr.java_conf.stardiopside.ktlearn.sound.event.SoundExceptionEvent
import jp.gr.java_conf.stardiopside.ktlearn.sound.event.SoundLineEvent
import jp.gr.java_conf.stardiopside.ktlearn.sound.service.SoundService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.function.BiPredicate

@SpringBootApplication
class Console(val service: SoundService) : CommandLineRunner {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    override fun run(vararg args: String?) {
        Arrays.stream(args).map { Paths.get(it) }
                .flatMap { Files.find(it, Integer.MAX_VALUE, BiPredicate { _, attr -> attr.isRegularFile }).sorted() }
                .forEach { service.play(it) }
    }

    @EventListener
    fun onSoundLineEvent(event: SoundLineEvent) = logger.info(event.lineEvent.toString())

    @EventListener
    fun onSoundActionEvent(event: SoundActionEvent) = logger.info(event.action)

    @EventListener
    fun onSoundExceptionEvent(event: SoundExceptionEvent) = logger.info(event.exception.message, event.exception)
}

fun main(args: Array<String>) {
    runApplication<Console>(*args)
}
