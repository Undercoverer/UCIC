package gay.extremist

val BASE_STORAGE_PATH = System.getenv("STORAGE_PATH") ?: "/app/data/storage"
val BASE_VIDEO_STORAGE_PATH = "$BASE_STORAGE_PATH/videos"
val TMP_VIDEO_STORAGE = "${BASE_VIDEO_STORAGE_PATH}/tmp"
