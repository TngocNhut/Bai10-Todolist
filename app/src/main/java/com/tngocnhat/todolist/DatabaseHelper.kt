package com.tngocnhat.todolist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "TodoListDB"
        private const val DATABASE_VERSION = 1

        // Users Table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"

        // Todos Table
        private const val TABLE_TODOS = "todos"
        private const val COLUMN_TODO_ID = "id"
        private const val COLUMN_TODO_USER_ID = "user_id"
        private const val COLUMN_TODO_TITLE = "title"
        private const val COLUMN_TODO_DESCRIPTION = "description"
        private const val COLUMN_TODO_COMPLETED = "is_completed"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create Users Table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()

        // Create Todos Table
        val createTodosTable = """
            CREATE TABLE $TABLE_TODOS (
                $COLUMN_TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TODO_USER_ID INTEGER NOT NULL,
                $COLUMN_TODO_TITLE TEXT NOT NULL,
                $COLUMN_TODO_DESCRIPTION TEXT,
                $COLUMN_TODO_COMPLETED INTEGER DEFAULT 0,
                FOREIGN KEY($COLUMN_TODO_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()

        db?.execSQL(createUsersTable)
        db?.execSQL(createTodosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TODOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // User Operations
    fun registerUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
        }
        
        return try {
            val result = db.insert(TABLE_USERS, null, values)
            result != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun loginUser(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_PASSWORD),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            )
        }
        cursor.close()
        db.close()
        return user
    }

    // Todo Operations
    fun addTodo(userId: Int, title: String, description: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TODO_USER_ID, userId)
            put(COLUMN_TODO_TITLE, title)
            put(COLUMN_TODO_DESCRIPTION, description)
            put(COLUMN_TODO_COMPLETED, 0)
        }

        return try {
            val result = db.insert(TABLE_TODOS, null, values)
            result != -1L
        } finally {
            db.close()
        }
    }

    fun getTodosByUser(userId: Int): List<Todo> {
        val todos = mutableListOf<Todo>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TODOS,
            null,
            "$COLUMN_TODO_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, "$COLUMN_TODO_ID DESC"
        )

        while (cursor.moveToNext()) {
            val todo = Todo(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TODO_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TODO_USER_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TODO_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TODO_DESCRIPTION)),
                isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TODO_COMPLETED)) == 1
            )
            todos.add(todo)
        }
        cursor.close()
        db.close()
        return todos
    }

    fun updateTodo(todoId: Int, title: String, description: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TODO_TITLE, title)
            put(COLUMN_TODO_DESCRIPTION, description)
        }

        return try {
            val result = db.update(TABLE_TODOS, values, "$COLUMN_TODO_ID = ?", arrayOf(todoId.toString()))
            result > 0
        } finally {
            db.close()
        }
    }

    fun deleteTodo(todoId: Int): Boolean {
        val db = writableDatabase
        return try {
            val result = db.delete(TABLE_TODOS, "$COLUMN_TODO_ID = ?", arrayOf(todoId.toString()))
            result > 0
        } finally {
            db.close()
        }
    }

    fun toggleTodoComplete(todoId: Int, isCompleted: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TODO_COMPLETED, if (isCompleted) 1 else 0)
        }

        return try {
            val result = db.update(TABLE_TODOS, values, "$COLUMN_TODO_ID = ?", arrayOf(todoId.toString()))
            result > 0
        } finally {
            db.close()
        }
    }
}
