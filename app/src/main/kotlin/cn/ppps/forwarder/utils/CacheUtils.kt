package cn.ppps.forwarder.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.math.BigDecimal

@Suppress("DEPRECATION")
class CacheUtils private constructor() {
    companion object {
        /**
         *
         */
        fun getTotalCacheSize(context: Context): String {
            return try {
                var cacheSize = getFolderSize(context.cacheDir)
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    cacheSize += getFolderSize(context.externalCacheDir)
                }
                getFormatSize(cacheSize.toDouble())
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CacheUtils", "getTotalCacheSize: ${e.message}")
                "0KB"
            }
        }

        /***
         */
        fun clearAllCache(context: Context) {
            deleteDir(context.cacheDir)
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                deleteDir(context.externalCacheDir)
            }
        }

        private fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()!!
                for (child in children) {
                    val success = deleteDir(File(dir, child))
                    if (!success) {
                        return false
                    }
                }
            }
            assert(dir != null)
            return dir!!.delete()
        }

        private fun getFolderSize(file: File?): Long {
            var size: Long = 0
            try {
                val fileList = file!!.listFiles()!!
                for (value in fileList) {
                    size = if (value.isDirectory) {
                        size + getFolderSize(value)
                    } else {
                        size + value.length()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CacheUtils", "getFolderSize: ${e.message}")
            }
            return size
        }

        /**
         *
         */
        private fun getFormatSize(size: Double): String {
            val kiloByte = size / 1024
            if (kiloByte < 1) {
                return "0KB"
            }
            val megaByte = kiloByte / 1024
            if (megaByte < 1) {
                val result1 = BigDecimal(kiloByte.toString())
                return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
            }
            val gigaByte = megaByte / 1024
            if (gigaByte < 1) {
                val result2 = BigDecimal(megaByte.toString())
                return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
            }
            val teraBytes = gigaByte / 1024
            if (teraBytes < 1) {
                val result3 = BigDecimal(gigaByte.toString())
                return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
            }
            val result4 = BigDecimal(teraBytes)
            return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
        }
    }
}