package com.tngocnhat.todolist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var rvTodos: RecyclerView
    private lateinit var emptyStateView: LinearLayout
    private lateinit var fabAddTodo: FloatingActionButton
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var todoAdapter: TodoAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get user session
        val sharedPrefs = getSharedPreferences("TodoListPrefs", Context.MODE_PRIVATE)
        userId = sharedPrefs.getInt("userId", -1)

        if (userId == -1) {
            // User not logged in, redirect to login
            navigateToLogin()
            return
        }

        // Initialize views
        rvTodos = findViewById(R.id.rvTodos)
        emptyStateView = findViewById(R.id.emptyStateView)
        fabAddTodo = findViewById(R.id.fabAddTodo)

        dbHelper = DatabaseHelper(this)

        // Setup RecyclerView
        setupRecyclerView()

        // Load todos
        loadTodos()

        fabAddTodo.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTodos()
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            todos = mutableListOf(),
            onDeleteClick = { todo ->
                showDeleteConfirmation(todo)
            },
            onToggleComplete = { todo, isCompleted ->
                dbHelper.toggleTodoComplete(todo.id, isCompleted)
                loadTodos()
            },
            onItemClick = { todo ->
                editTodo(todo)
            }
        )

        rvTodos.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = todoAdapter
        }
    }

    private fun loadTodos() {
        val todos = dbHelper.getTodosByUser(userId)
        
        if (todos.isEmpty()) {
            rvTodos.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
        } else {
            rvTodos.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
            todoAdapter.updateTodos(todos)
        }
    }

    private fun showDeleteConfirmation(todo: Todo) {
        AlertDialog.Builder(this)
            .setTitle("Delete Todo")
            .setMessage("Are you sure you want to delete '${todo.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                val success = dbHelper.deleteTodo(todo.id)
                if (success) {
                    Toast.makeText(this, "Todo deleted", Toast.LENGTH_SHORT).show()
                    todoAdapter.removeTodo(todo)
                    loadTodos()
                } else {
                    Toast.makeText(this, "Failed to delete todo", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editTodo(todo: Todo) {
        val intent = Intent(this, AddTodoActivity::class.java).apply {
            putExtra("todoId", todo.id)
            putExtra("todoTitle", todo.title)
            putExtra("todoDescription", todo.description)
        }
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
