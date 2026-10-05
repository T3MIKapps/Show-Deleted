package com.example.msgsaver

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Msg(val pkg: String, val sender: String, val text: String, val time: Long, val deleted: Boolean)

class MessageStore(ctx: Context) : SQLiteOpenHelper(ctx, "messages.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pkg TEXT, sender TEXT, text TEXT, time INTEGER, deleted INTEGER DEFAULT 0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, o: Int, n: Int) {}

    /** Сохраняет сообщение, если оно не дубликат последнего от этого контакта. */
    fun insert(pkg: String, sender: String, text: String, time: Long) {
        val last = writableDatabase.rawQuery(
            "SELECT text FROM messages WHERE pkg=? AND sender=? ORDER BY id DESC LIMIT 1",
            arrayOf(pkg, sender)
        ).use { if (it.moveToFirst()) it.getString(0) else null }
        if (last == text) return
        val v = ContentValues().apply {
            put("pkg", pkg); put("sender", sender); put("text", text); put("time", time)
        }
        writableDatabase.insert("messages", null, v)
    }

    /** Помечает последнее сохранённое сообщение контакта как удалённое. */
    fun markLastDeleted(pkg: String, sender: String) {
        writableDatabase.execSQL(
            "UPDATE messages SET deleted=1 WHERE id=(SELECT id FROM messages " +
                "WHERE pkg=? AND sender=? AND deleted=0 ORDER BY id DESC LIMIT 1)",
            arrayOf(pkg, sender)
        )
    }

    fun all(onlyDeleted: Boolean): List<Msg> {
        val where = if (onlyDeleted) "WHERE deleted=1" else ""
        val out = mutableListOf<Msg>()
        readableDatabase.rawQuery(
            "SELECT pkg, sender, text, time, deleted FROM messages $where ORDER BY id DESC LIMIT 500", null
        ).use { c ->
            while (c.moveToNext()) out += Msg(c.getString(0), c.getString(1), c.getString(2), c.getLong(3), c.getInt(4) == 1)
        }
        return out
    }
}
