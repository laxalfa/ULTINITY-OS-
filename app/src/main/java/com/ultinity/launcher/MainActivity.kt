package com.ultinity.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 10, 16)
    private val card = Color.rgb(17, 25, 36)
    private val cyan = Color.rgb(0, 229, 255)
    private val white = Color.rgb(242, 247, 255)
    private val muted = Color.rgb(145, 160, 180)

    private lateinit var appList: LinearLayout
    private lateinit var search: EditText

    private data class AppItem(
        val name: String,
        val pkg: String,
        val icon: android.graphics.drawable.Drawable
    )

    private var apps = listOf<AppItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        apps = loadApps()
        buildScreen()
    }

    override fun onResume() {
        super.onResume()
        if (::appList.isInitialized) {
            apps = loadApps()
            renderApps(search.text.toString())
        }
    }

    private fun loadApps(): List<AppItem> {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)

        return packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { info ->
                val ai = info.activityInfo ?: return@mapNotNull null
                if (ai.packageName == packageName) return@mapNotNull null

                AppItem(
                    info.loadLabel(packageManager).toString(),
                    ai.packageName,
                    info.loadIcon(packageManager)
                )
            }
            .distinctBy { it.pkg }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(12))
            setBackgroundColor(bg)
        }

        root.addView(text("ULTINITY", 27f, white, true))
        root.addView(text(
            "LAUNCHER  /  GAMING INTERFACE",
            10f, cyan, true
        ).apply {
            setPadding(0, dp(3), 0, dp(18))
        })

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(card, cyan, 1, 20)
            setPadding(dp(16), dp(18), dp(16), dp(18))
        }
        hero.addView(text("YOUR SPACE. YOUR RULES.", 11f, cyan, true))
        hero.addView(text(
            "Level up your\nAndroid experience.",
            25f, white, true
        ).apply { setPadding(0, dp(8), 0, dp(8)) })
        hero.addView(text(
            "A cleaner home for your games and apps.",
            12f, muted, false
        ))
        root.addView(hero)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(14))
        }

        actions.addView(actionButton("SHIELD") {
            showHidden()
        }, LinearLayout.LayoutParams(0, dp(44), 1f))

        actions.addView(View(this), LinearLayout.LayoutParams(dp(8), 1))

        actions.addView(actionButton("SETTINGS") {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }, LinearLayout.LayoutParams(0, dp(44), 1f))

        root.addView(actions)
        root.addView(text("YOUR APPS", 12f, muted, true))

        search = EditText(this).apply {
            hint = "Search installed apps..."
            setHintTextColor(muted)
            setTextColor(white)
            textSize = 14f
            singleLine = true
            setPadding(dp(12), 0, dp(12), 0)
            background = rounded(card, Color.DKGRAY, 1, 12)
        }

        root.addView(search, LinearLayout.LayoutParams(
            -1, dp(48)
        ).apply {
            topMargin = dp(10)
            bottomMargin = dp(10)
        })

        val scroll = ScrollView(this)
        appList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(appList)
        root.addView(scroll, LinearLayout.LayoutParams(
            -1, 0, 1f
        ))

        root.addView(text(
            "ULTINITY  •  BUILT FOR FOCUS",
            10f, muted, true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(4))
        })

        setContentView(root)

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int,
                before: Int, count: Int
            ) {
                renderApps(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        renderApps("")
    }

    private fun renderApps(query: String) {
        appList.removeAllViews()
        val hidden = getHidden()
        val visible = apps.filter {
            it.pkg !in hidden &&
                it.name.contains(query.trim(), ignoreCase = true)
        }

        if (visible.isEmpty()) {
            appList.addView(text(
                "No matching apps found.",
                13f, muted, false
            ).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(24), 0, dp(24))
            })
            return
        }

        visible.forEach { item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = rounded(card, Color.rgb(35, 47, 62), 1, 14)
                setPadding(dp(10), dp(8), dp(10), dp(8))
                isClickable = true
                isFocusable = true
                setOnClickListener { openApp(item) }
                setOnLongClickListener {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Hide ${item.name}?")
                        .setMessage(
                            "This hides the app in ULTINITY only. " +
                            "It does not uninstall or secure the app."
                        )
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Hide") { _, _ ->
                            val updated = getHidden().toMutableSet()
                            updated.add(item.pkg)
                            saveHidden(updated)
                            renderApps(search.text.toString())
                        }
                        .show()
                    true
                }
            }

            row.addView(ImageView(this).apply {
                setImageDrawable(item.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }, LinearLayout.LayoutParams(dp(42), dp(42)))

            val details = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
            }
            details.addView(text(item.name, 14f, white, true))
            details.addView(text(
                "Tap to open  •  Hold to hide",
                10f, muted, false
            ))

            row.addView(details, LinearLayout.LayoutParams(
                0, -2, 1f
            ))
            appList.addView(row, LinearLayout.LayoutParams(
                -1, dp(64)
            ).apply {
                bottomMargin = dp(6)
            })
        }
    }

    private fun openApp(item: AppItem) {
        try {
            val launch = packageManager
                .getLaunchIntentForPackage(item.pkg)
            if (launch != null) startActivity(launch)
            else toast("This app cannot be opened from its launcher entry.")
        } catch (_: Exception) {
            toast("Unable to open this app.")
        }
    }

    private fun showHidden() {
        val hidden = apps.filter { it.pkg in getHidden() }

        if (hidden.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("ULTINITY Shield")
                .setMessage(
                    "No hidden apps yet. Hold an app in the list to hide it."
                )
                .setPositiveButton("OK", null)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Hidden apps — tap to restore")
            .setItems(hidden.map { it.name }.toTypedArray()) { _, index ->
                val updated = getHidden().toMutableSet()
                updated.remove(hidden[index].pkg)
                saveHidden(updated)
                renderApps(search.text.toString())
                toast("App restored to ULTINITY.")
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun getHidden(): Set<String> =
        getSharedPreferences("ultinity", MODE_PRIVATE)
            .getStringSet("hidden", emptySet())
            ?.toSet() ?: emptySet()

    private fun saveHidden(items: Set<String>) {
        getSharedPreferences("ultinity", MODE_PRIVATE)
            .edit()
            .putStringSet("hidden", items.toSet())
            .apply()
    }

    private fun actionButton(
        title: String,
        action: () -> Unit
    ): TextView = text(title, 12f, cyan, true).apply {
        gravity = Gravity.CENTER
        background = rounded(card, cyan, 1, 12)
        isClickable = true
        isFocusable = true
        setOnClickListener { action() }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }

    private fun rounded(
        color: Int,
        stroke: Int,
        width: Int,
        radius: Int
    ) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (width > 0) setStroke(dp(width), stroke)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun toast(value: String) =
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
}
