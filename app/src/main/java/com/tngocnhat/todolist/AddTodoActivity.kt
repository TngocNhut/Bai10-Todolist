package com.tngocnhat.todolist

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddTodoActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton
    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1
    private var todoId: Int = -1
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)

        // Get user session
        val sharedPrefs = getSharedPreferences("TodoListPrefs", Context.MODE_PRIVATE)
        userId = sharedPrefs.getInt("userId", -1)

        // Initialize views
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        dbHelper = DatabaseHelper(this)

        // Check if editing existing todo
        todoId = intent.getIntExtra("todoId", -1)
        if (todoId != -1) {
            isEditMode = true
            etTitle.setText(intent.getStringExtra("todoTitle"))
            etDescription.setText(intent.getStringExtra("todoDescription"))
        }

        btnSave.setOnClickListener {
            saveTodo()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun saveTodo() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        val success = if (isEditMode) {
            dbHelper.updateTodo(todoId, title, description)
        } else {
            dbHelper.addTodo(userId, title, description)
        }

        if (success) {
            val message = if (isEditMode) "Todo updated!" else "Todo added!"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save todo", Toast.LENGTH_SHORT).show()
        }
    }
}
