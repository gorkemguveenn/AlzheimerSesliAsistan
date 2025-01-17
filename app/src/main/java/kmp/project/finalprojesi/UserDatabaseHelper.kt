package kmp.project.finalprojesi

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "userDatabase.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "users"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun isValidUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?", arrayOf(email, password))
        val isValid = cursor.moveToFirst() // Kullanıcı bulunduysa geçerli
        cursor.close()
        return isValid
    }

    fun insertPhoneNumber(phoneNumber: String): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("phone_number", phoneNumber)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getPhoneNumber(): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT phone_number FROM $TABLE_NAME LIMIT 1", null)
        var phoneNumber: String? = null
        if (cursor.moveToFirst()) {
            phoneNumber = cursor.getString(cursor.getColumnIndex("phone_number"))
        }
        cursor.close()
        return phoneNumber
    }
}
