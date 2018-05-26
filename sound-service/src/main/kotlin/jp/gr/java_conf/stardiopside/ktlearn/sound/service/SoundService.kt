package jp.gr.java_conf.stardiopside.ktlearn.sound.service

import java.io.InputStream
import java.nio.file.Path
import java.time.Duration

interface SoundService {
    val position: Duration?
    fun play(path: Path): Boolean
    fun play(inputStream: InputStream, name: String?): Boolean
    fun skip()
}
