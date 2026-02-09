package com.huma.app.data.local

import androidx.room.TypeConverter
import com.google.gson.*
import com.huma.app.ui.screen.note.NoteBlock
import com.huma.app.ui.screen.note.CheckItem

class NoteConverters {
    private val gson = GsonBuilder().create()

    @TypeConverter
    fun fromNoteBlockList(blocks: List<NoteBlock>): String {
        val array = JsonArray()
        blocks.forEach { block ->
            val obj = JsonObject()
            when (block) {
                is NoteBlock.Text -> {
                    obj.addProperty("type", "text")
                    obj.addProperty("content", block.content)
                }
                is NoteBlock.Heading -> {
                    obj.addProperty("type", "heading")
                    obj.addProperty("content", block.content)
                }
                is NoteBlock.BulletList -> {
                    obj.addProperty("type", "bullet")
                    obj.add("items", gson.toJsonTree(block.items))
                }
                is NoteBlock.CheckboxGroup -> {
                    obj.addProperty("type", "check")
                    obj.add("items", gson.toJsonTree(block.items))
                }
            }
            array.add(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toNoteBlockList(data: String): List<NoteBlock> {
        val list = mutableListOf<NoteBlock>()
        val array = JsonParser.parseString(data).asJsonArray
        array.forEach { element ->
            val obj = element.asJsonObject
            val type = obj.get("type").asString
            when (type) {
                "text" -> list.add(NoteBlock.Text(obj.get("content").asString))
                "heading" -> list.add(NoteBlock.Heading(obj.get("content").asString))
                "bullet" -> {
                    val items: MutableList<String> = gson.fromJson(obj.get("items"), object : com.google.gson.reflect.TypeToken<MutableList<String>>() {}.type)
                    list.add(NoteBlock.BulletList(items))
                }
                "check" -> {
                    val items: MutableList<CheckItem> = gson.fromJson(obj.get("items"), object : com.google.gson.reflect.TypeToken<MutableList<CheckItem>>() {}.type)
                    list.add(NoteBlock.CheckboxGroup(items))
                }
            }
        }
        return list
    }
}