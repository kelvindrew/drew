package com.example.kasa.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.kasa.MainActivity
import com.example.kasa.R
import com.example.kasa.data.repository.KasaStorage

class KasaHomeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_kasa_home)
            val storage = KasaStorage(context)

            val hh = storage.loadHousehold()
            val members = storage.loadMembers()
            val mealPlan = storage.loadMealPlan()

            // 1. Household Name
            val hhName = hh?.name ?: "Mon Foyer"
            views.setTextViewText(R.id.widget_household_name, "🏡 $hhName")

            // 2. Members at home presence
            val membersAtHome = members.filter { it.isHome }
            val presenceText = if (membersAtHome.isNotEmpty()) {
                val names = membersAtHome.joinToString(", ") { it.name }
                "${membersAtHome.size} à la maison : $names"
            } else {
                "Tous dehors (aucun à la maison)"
            }
            views.setTextViewText(R.id.widget_presence_text, presenceText)

            // 3. Evening meal
            val mealText = when {
                mealPlan != null && mealPlan.dinnerTitle.isNotBlank() -> {
                    val chef = if (!mealPlan.chefUserName.isNullOrBlank()) " (Chef: ${mealPlan.chefUserName})" else ""
                    "Ce soir : ${mealPlan.dinnerTitle}$chef"
                }
                mealPlan != null && mealPlan.lunchTitle.isNotBlank() -> {
                    "Ce midi : ${mealPlan.lunchTitle}"
                }
                else -> {
                    "Ce soir : Menu libre ou à définir"
                }
            }
            views.setTextViewText(R.id.widget_meal_text, mealText)

            // 4. Click opens MainActivity
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, KasaHomeWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(componentName)
                if (ids.isNotEmpty()) {
                    val intent = Intent(context, KasaHomeWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                    context.sendBroadcast(intent)
                }
            } catch (e: Exception) {
                // Ignore widget update errors gracefully
            }
        }
    }
}
