package com.huma.app.ui.widget

import android.content.Context
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.huma.app.MainActivity
import com.huma.app.R
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.TaskPriority
import android.app.PendingIntent
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*

class TaskWidgetFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {



    private var tasks = listOf<com.huma.app.data.local.TaskEntity>()

    override fun onCreate() {}

    override fun onDataSetChanged() {

        val db = AppDatabase.getInstance(context)

        val identityToken = android.os.Binder.clearCallingIdentity()

        try {
            tasks = db.taskDao().getTodayTasksWidget()
        } finally {
            android.os.Binder.restoreCallingIdentity(identityToken)
        }
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {

        val task = tasks[position]

        val views = RemoteViews(
            context.packageName,
            R.layout.widget_task_item
        )

        // ================= TEXT =================

        views.setTextViewText(R.id.taskTitle, task.title)

        views.setTextViewText(
            R.id.taskDescription,
            task.description ?: ""
        )

        val format = SimpleDateFormat("dd MMM", Locale.getDefault())

        val start = format.format(Date(task.startDate))

        val time = task.dueDate ?: ""

        views.setTextViewText(
            R.id.taskDate,
            "$start  $time"
        )

        val moodText = when (task.mood) {
            com.huma.app.data.local.TaskMood.CALM -> "😌 Calm"
            com.huma.app.data.local.TaskMood.NORMAL -> "🙂 Normal"
            com.huma.app.data.local.TaskMood.STRESS -> "😵 Stress"
        }

        views.setTextViewText(
            R.id.taskMood,
            moodText
        )

        // ================= PRIORITY =================

        val color = when (task.priority) {
            TaskPriority.HIGH -> "#FF6B6B"
            TaskPriority.MEDIUM -> "#FFC75F"
            TaskPriority.LOW -> "#4D96FF"
        }

        views.setInt(
            R.id.priorityDot,
            "setColorFilter",
            android.graphics.Color.parseColor(color)
        )

        // ================= CHECKBOX =================

        val icon =
            if (task.isDone)
                R.drawable.widget_checkbox_checked
            else
                R.drawable.widget_checkbox_unchecked

        views.setImageViewResource(
            R.id.checkIcon,
            icon
        )

        // ================= CHECKBOX ACTION =================

        val checkIntent = Intent()
        checkIntent.action = "CHECK_TASK"
        checkIntent.putExtra("taskId", task.id)

        views.setOnClickFillInIntent(
            R.id.checkIcon,
            checkIntent
        )


        // ================= OPEN APP =================

        val openIntent = Intent()
        openIntent.action = "OPEN_APP"
        openIntent.putExtra("taskId", task.id)

        views.setOnClickFillInIntent(
            R.id.taskRoot,
            openIntent
        )

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
    override fun onDestroy() {}

}

