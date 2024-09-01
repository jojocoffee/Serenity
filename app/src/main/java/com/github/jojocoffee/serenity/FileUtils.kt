package com.github.jojocoffee.serenity

import android.content.Context
import com.google.gson.Gson
import java.io.File

object FileUtils {
    private const val DEFAULT_FILE_NAME = "meditatedDaysStorage.json"

    fun saveListToFile(context: Context, list: List<String>, fileName: String = DEFAULT_FILE_NAME) {
        val gson = Gson()
        val jsonString = gson.toJson(list)
        val file = File(context.filesDir, fileName)
        file.writeText(jsonString)
    }

    fun readListFromFile(context: Context, fileName: String = DEFAULT_FILE_NAME): MutableList<String> {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            val jsonString = file.readText()
            val gson = Gson()
            gson.fromJson(jsonString, Array<String>::class.java).toMutableList()
        } else {
            mutableListOf()
        }
    }
}