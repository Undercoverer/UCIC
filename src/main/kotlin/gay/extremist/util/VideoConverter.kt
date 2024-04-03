package gay.extremist.util

import java.io.File
import java.util.concurrent.CompletableFuture


class VideoConverter {
    companion object {
        fun convert(inputFile: String, outputDir: String, newFileNames: String = "output"): String {
            if (!isFFmpegInstalled()) {
                println("Error: FFmpeg is not installed")
                return "Error"
            }

            val input = File(inputFile).canonicalFile.absoluteFile
            val output = File(outputDir).canonicalFile.absoluteFile
            output.mkdirs()

            var resolutions = listOf(
                "360", "480", "720", "1080", "1440"
            )

            // Remove resolutions higher than the input video
            //            val process = Runtime.getRuntime().exec("ffprobe -v error -select_streams v:0 -show_entries stream=height -of csv=s=x:p=0 $input").
            val processBuilder = ProcessBuilder(
                "ffprobe",
                "-v",
                "error",
                "-select_streams",
                "v:0",
                "-show_entries",
                "stream=height",
                "-of",
                "csv=s=x:p=0",
                input.absolutePath
            )
            val process = processBuilder.start().also { it.waitFor() }
            val procOutput = process.inputReader().lines().toList().joinToString { it }.trim()

            val inputHeight = procOutput.toIntOrNull() ?: return "Error"


            resolutions = resolutions.filter { it.toInt() <= inputHeight }
            if (resolutions.isEmpty()) {
                resolutions = listOf("360")
            }


            val futures = mutableListOf<CompletableFuture<Process>>()
            resolutions.forEach {
                val outputFileName = "${newFileNames}_${it}.webm"
                val outputFilePath = File(output, outputFileName)

                val command = arrayOf(
                    "ffmpeg",
                    "-i",
                    input.absolutePath,
                    "-c:v",
                    "libvpx-vp9",
                    "-keyint_min",
                    "150",
                    "-g",
                    "150",
                    "-tile-columns",
                    "4",
                    "-frame-parallel",
                    "1",
                    "-f",
                    "webm",
                    "-dash",
                    "1",
                    "-an",
                    "-vf",
                    "scale=-1:$it",
                    outputFilePath.absolutePath
                )

                futures.add(executeCommand(command)!!)
            }

            val audioOutputFile = File(output, "${newFileNames}_audio_128k.webm")
            val audioCommand = arrayOf(
                "ffmpeg",
                "-i",
                input.absolutePath,
                "-vn",
                "-acodec",
                "libvorbis",
                "-ab",
                "128k",
                "-dash",
                "1",
                audioOutputFile.absolutePath
            )

            futures.add(executeCommand(audioCommand)!!)

            val manifestResSwitches = resolutions.flatMap {
                listOf("-f", "webm_dash_manifest", "-i", "${output}/${newFileNames}_$it.webm")
            }.toTypedArray()

            val copyMapSwitches = (0..resolutions.size).flatMap {
                listOf("-map", "$it")
            }.toTypedArray()

            val manifestCommand: List<String> = listOf(
                "ffmpeg",
                *manifestResSwitches,
                "-f",
                "webm_dash_manifest",
                "-i",
                "${output}/${newFileNames}_audio_128k.webm",
                "-c",
                "copy",
                *copyMapSwitches,
                "-f",
                "webm_dash_manifest",
                "-adaptation_sets",
                "id=0,streams=${resolutions.indices.joinToString(",")} id=1,streams=${resolutions.size}",
                "-chunk_duration_ms",
                "5000",
                "${output}/$newFileNames.mpd"
            )

            val completionService = CompletableFuture.allOf(*futures.toTypedArray())
            completionService.join()
            executeCommand(manifestCommand.toTypedArray())!!.join()

            return "Success"
        }

        private fun isFFmpegInstalled(): Boolean {
            val process = ProcessBuilder("ffmpeg", "-version").start()
            val exitCode = process.waitFor()
            return exitCode == 0
        }

        private fun executeCommand(command: Array<String>): CompletableFuture<Process>? {
            val process = ProcessBuilder(*command).start()
            return process.onExit()
        }
    }
}