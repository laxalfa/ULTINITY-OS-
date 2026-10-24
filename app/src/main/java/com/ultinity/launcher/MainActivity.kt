package com.ultinity.launcher

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
                    val t = TextView(this)
                            t.text = "ULTINITY"
                                    t.setTextColor(Color.parseColor("#00E5FF"))
                                            t.setBackgroundColor(Color.BLACK)
                                                    t.textSize = 32f
                                                            t.gravity = Gravity.CENTER
                                                                    setContentView(t)
                                                                        }
                                                                        }