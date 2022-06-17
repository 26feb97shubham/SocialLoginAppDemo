package com.login.socialloginappdemo

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder

object Utils {
    @Throws(IOException::class)
    fun streamToString(`is`: InputStream?): String? {
        var str = ""
        if (`is` != null) {
            val sb = StringBuilder()
            var line: String?
            try {
                val reader = BufferedReader(
                    InputStreamReader(`is`)
                )
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
            } finally {
                `is`.close()
            }
            str = sb.toString()
        }
        return str
    }
}