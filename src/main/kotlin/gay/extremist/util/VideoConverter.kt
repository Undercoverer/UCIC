import java.io.File
import java.util.concurrent.CompletableFuture


class VideoConverter {
    fun convert(inputFile: String, outputDir: String, speed: Int) {
        if (!isFFmpegInstalled()) {
            println("Error: FFmpeg is not installed")
            return
        }

        val homeDir = System.getProperty("user.home")
        val input = File(inputFile.replace("~", homeDir))
        val fileName = input.nameWithoutExtension
        val output = File(outputDir.replace("~", homeDir))
        output.mkdirs()

        var resolutions = listOf(
            "360", "480", "720", "1080", "1440"
        )

        // Remove resolutions higher than the input video
        val process = Runtime.getRuntime().exec("ffprobe -v error -select_streams v:0 -show_entries stream=height -of csv=s=x:p=0 $inputFile")
            .inputStream.bufferedReader().readLine()
        val inputHeight = process.toIntOrNull() ?: throw Exception("Invalid input file")
        resolutions = resolutions.filter { it.toInt() > inputHeight }


        val futures = mutableListOf<CompletableFuture<Process>>()
        resolutions.forEach {
            val outputFileName = "${fileName}_${it}.webm"
            val outputFilePath = File(output, outputFileName)

            val command = arrayOf(
                "ffmpeg",
                "-i", input.absolutePath,
                "-c:v", "libvpx-vp9", "-speed", "$speed",
                "-keyint_min", "150", "-g", "150", "-tile-columns", "4", "-frame-parallel", "1", "-f", "webm",
                "-dash", "1", "-an", "-vf", "scale=-1:$it", outputFilePath.absolutePath
            )

            futures.add(executeCommand(command)!!)
        }

        val audioOutputFile = File(output, "${fileName}_audio_128k.webm")
        val audioCommand = arrayOf(
            "ffmpeg", "-i", input.absolutePath,
            "-vn", "-acodec", "libvorbis", "-ab", "128k",
            "-dash", "1", audioOutputFile.absolutePath
        )

        futures.add(executeCommand(audioCommand)!!)

        val manifestCommand = arrayOf(
            "ffmpeg",
            "-f", "webm_dash_manifest", "-i", "${output}/${fileName}_360.webm",
            "-f", "webm_dash_manifest", "-i", "${output}/${fileName}_480.webm",
            "-f", "webm_dash_manifest", "-i", "${output}/${fileName}_720.webm",
            "-f", "webm_dash_manifest", "-i", "${output}/${fileName}_1080.webm",
            "-f", "webm_dash_manifest", "-i", "${output}/${fileName}_1440.webm",
            "-f", "webm_dash_manifest", "-i", "${output}/${fileName}_audio_128k.webm",
            "-c", "copy",
            "-map", "0", "-map", "1", "-map", "2", "-map", "3", "-map", "4", "-map", "5",
            "-f", "webm_dash_manifest",
            "-adaptation_sets", "id=0,streams=0,1,2,3,4 id=1,streams=5",
            "-chunk_duration_ms", "5000",
            "${output}/${fileName}.mpd"
        )

        val completionService = CompletableFuture.allOf(*futures.toTypedArray())
        completionService.join()
        executeCommand(manifestCommand)!!.join()

        println("Encoding completed successfully!")
    }

    private fun isFFmpegInstalled(): Boolean {
        val process = ProcessBuilder("ffmpeg", "-version").start()
        val exitCode = process.waitFor()
        return exitCode == 0
    }

    private fun executeCommand(command: Array<String>, ): CompletableFuture<Process>? {
        val process = ProcessBuilder(*command).inheritIO().start()
        return process.onExit()
    }
}