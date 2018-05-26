package jp.gr.java_conf.stardiopside.ktlearn.sound.service

import jp.gr.java_conf.stardiopside.ktlearn.sound.event.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.temporal.ChronoUnit
import javax.sound.sampled.*

@Service
class SoundServiceImpl(val publisher: ApplicationEventPublisher) : SoundService {
    companion object {
        val logger = LoggerFactory.getLogger(this::class.java.enclosingClass)!!
    }

    final override var position: Duration? = null
        private set

    @Volatile
    private var skipping: Boolean = false

    override fun play(path: Path): Boolean {
        try {
            Files.newInputStream(path).use {
                return play(it, path.fileName.toString())
            }
        } catch (e: IOException) {
            logger.warn(e.message, e)
            publisher.publishEvent(SoundExceptionEvent(e))
            return false
        }
    }

    override fun play(inputStream: InputStream, name: String?): Boolean {
        val title = name ?: "unnamed"
        publisher.publishEvent(SoundActionEvent("Begin $title"))
        try {
            val input = if (inputStream.markSupported()) inputStream else inputStream.buffered()
            getTitle(input)?.let { publisher.publishEvent(SoundActionEvent("Title: $it")) }
            play(input)
            return true
        } catch (e: Exception) {
            when (e) {
                is IOException, is UnsupportedAudioFileException, is LineUnavailableException -> {
                    logger.warn(e.message, e)
                    publisher.publishEvent(SoundExceptionEvent(e))
                    return false
                }
                else -> throw e
            }
        } finally {
            publisher.publishEvent(SoundActionEvent("End $title"))
        }
    }

    private fun play(inputStream: InputStream) {
        assert(inputStream.markSupported())
        AudioSystem.getAudioInputStream(inputStream).use {
            val baseFormat = it.format
            publisher.publishEvent(SoundActionEvent("INPUT: ${baseFormat.javaClass} - $baseFormat"))
            if (baseFormat.encoding == AudioFormat.Encoding.PCM_SIGNED || baseFormat.encoding == AudioFormat.Encoding.PCM_UNSIGNED) {
                play(it, baseFormat)
            } else {
                val decodedFormat = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.sampleRate, 16, baseFormat.channels, baseFormat.channels * 2, baseFormat.sampleRate, false)
                publisher.publishEvent(SoundActionEvent("DECODED: ${decodedFormat.javaClass} - $decodedFormat"))
                AudioSystem.getAudioInputStream(decodedFormat, it).use {
                    play(it, decodedFormat)
                }
            }
        }
    }

    private fun play(inputStream: AudioInputStream, format: AudioFormat) {
        try {
            AudioSystem.getSourceDataLine(format).use {
                skipping = false
                it.addLineListener { publisher.publishEvent(SoundLineEvent(it)) }
                it.open(format)
                it.start()
                val data = ByteArray(it.bufferSize)
                var size = inputStream.read(data, 0, data.size)
                while (!skipping && size != -1) {
                    it.write(data, 0, size)
                    val pos = Duration.of(it.microsecondPosition, ChronoUnit.MICROS)
                    position = pos
                    publisher.publishEvent(SoundPositionEvent(pos))
                    size = inputStream.read(data, 0, data.size)
                }
                it.drain()
                it.stop()
                position = null
                publisher.publishEvent(SoundPositionFinishEvent)
            }
        } finally {
            skipping = false
        }
    }

    private fun getTitle(inputStream: InputStream): String? {
        assert(inputStream.markSupported())
        val audioFileFormat = AudioSystem.getAudioFileFormat(inputStream)
        val title = audioFileFormat.properties()["title"]
        return title?.toString()
    }

    override fun skip() {
        skipping = true
    }
}
