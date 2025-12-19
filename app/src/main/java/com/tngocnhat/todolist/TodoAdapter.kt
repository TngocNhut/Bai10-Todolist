package com.tngocnhat.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private var todos: MutableList<Todo>,
    private val onDeleteClick: (Todo) -> Unit,
    private val onToggleComplete: (Todo, Boolean) -> Unit,
    private val onItemClick: (Todo) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val cbComplete: CheckBox = itemView.findViewById(R.id.cbComplete)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]

        holder.tvTitle.text = todo.title
        holder.tvDescription.text = todo.description
        holder.cbComplete.isChecked = todo.isCompleted

        // Strike through completed todos
        if (todo.isCompleted) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.cbComplete.setOnCheckedChangeListener { _, isChecked ->
            onToggleComplete(todo, isChecked)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(todo)
        }

        holder.itemView.setOnClickListener {
            onItemClick(todo)
        }
    }

    override fun getItemCount(): Int = todos.size

    fun updateTodos(newTodos: List<Todo>) {
        todos.clear()
        todos.addAll(newTodos)
        notifyDataSetChanged()
    }

    fun removeTodo(todo: Todo) {
        val position = todos.indexOf(todo)
        if (position != -1) {
            todos.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
