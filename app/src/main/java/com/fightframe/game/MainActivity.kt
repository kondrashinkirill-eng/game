package com.fightframe.game

import android.app.Activity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.max
import kotlin.math.min

class MainActivity : Activity() {
    private lateinit var arena: ArenaView
    private lateinit var status: TextView
    private lateinit var stats: TextView
    private var hp = 100
    private var enemyHp = 100
    private var stamina = 100
    private var round = 1
    private var wins = 0
    private var level = 1
    private var strength = 12
    private var maxHp = 100
    private var move = "Стоять"
    private var action = "Удар рукой"
    private var distance = "Ближняя"
    private var resolving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 21, 31)
        window.navigationBarColor = Color.rgb(16, 21, 31)
        buildUi()
    }

    private fun buildUi() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.rgb(16, 21, 31))
        }
        val title = TextView(this).apply {
            text = "FIGHT//FRAME"
            textSize = 25f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        }
        page.addView(title)
        page.addView(TextView(this).apply {
            text = "TACTICAL FIGHTING · OFFLINE DEMO"
            textSize = 11f
            setTextColor(Color.rgb(151, 168, 188))
        })
        stats = TextView(this).apply { textSize = 14f; setTextColor(Color.WHITE); setPadding(0, dp(10), 0, dp(8)) }
        page.addView(stats)
        arena = ArenaView()
        page.addView(arena, LinearLayout.LayoutParams(-1, 0, 1f))
        status = TextView(this).apply {
            text = "Выбери дистанцию и запрограммируй действие."
            textSize = 14f
            setTextColor(Color.rgb(214, 226, 239))
            setPadding(0, dp(10), 0, dp(8))
        }
        page.addView(status)
        page.addView(label("ДИСТАНЦИЯ"))
        page.addView(buttonRow(listOf("Ближняя", "Дальняя")) { distance = it; arena.invalidate() })
        page.addView(label("ДВИЖЕНИЕ"))
        page.addView(buttonRow(listOf("Стоять", "Подойти", "Отойти")) { move = it; arena.invalidate() })
        page.addView(label("ДЕЙСТВИЕ"))
        page.addView(buttonRow(listOf("Удар рукой", "Удар ногой")) { action = it })
        page.addView(buttonRow(listOf("Блок", "Энергия")) { action = it })
        val fight = Button(this).apply {
            text = "ЗАМЕДЛИТЬ ВРЕМЯ · ВЫПОЛНИТЬ ХОД"
            setOnClickListener { resolveRound() }
        }
        page.addView(fight, LinearLayout.LayoutParams(-1, dp(52)))
        val reset = Button(this).apply {
            text = "НОВЫЙ БОЙ / СБРОС"
            setOnClickListener { resetGame() }
        }
        page.addView(reset, LinearLayout.LayoutParams(-1, dp(44)))
        setContentView(page)
        updateStats()
    }

    private fun label(s: String) = TextView(this).apply {
        text = s
        textSize = 11f
        setTextColor(Color.rgb(143, 162, 184))
        setPadding(0, dp(5), 0, dp(3))
    }

    private fun buttonRow(items: List<String>, picked: (String) -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            items.forEach { item ->
                val b = Button(this@MainActivity).apply {
                    text = item
                    textSize = 11f
                    setOnClickListener { picked(item); Toast.makeText(this@MainActivity, item, Toast.LENGTH_SHORT).show() }
                }
                addView(b, LinearLayout.LayoutParams(0, dp(44), 1f).apply { setMargins(dp(2), dp(1), dp(2), dp(1)) })
            }
        }
    }

    private fun resolveRound() {
        if (resolving) return
        if (hp <= 0 || enemyHp <= 0) { resetGame(); return }
        resolving = true
        val enemyAction = when (round % 3) { 0 -> "Блок"; 1 -> "Удар рукой"; else -> "Удар ногой" }
        val enemyMove = if (round % 2 == 0) "Стоять" else "Подойти"
        val effectiveDistance = when (move) {
            "Подойти" -> "Ближняя"
            "Отойти" -> "Дальняя"
            else -> distance
        }
        val cost = when (action) { "Удар рукой" -> 6; "Удар ногой" -> 12; "Энергия" -> 25; else -> 0 }
        var result = ""
        if (stamina < cost) {
            result = "Недостаточно энергии: действие сорвано."
            stamina = min(100, stamina + 12)
        } else if (action == "Блок") {
            hp = max(0, hp - if (enemyAction == "Блок") 0 else 2)
            stamina = max(0, stamina - 2)
            result = "Защита! Получен только небольшой урон усталости."
        } else if (enemyAction == "Блок") {
            stamina = max(0, stamina - cost - 4)
            result = "Соперник поставил блок. Удар не прошёл."
        } else if (effectiveDistance == "Дальняя" && action != "Энергия") {
            stamina = max(0, stamina - cost - 3)
            result = "Промах по дистанции! Обычный удар не достал."
        } else {
            val damage = when (action) { "Удар ногой" -> 18; "Энергия" -> 25; else -> 12 } + (strength - 12)
            enemyHp = max(0, enemyHp - damage)
            stamina = min(100, max(0, stamina - cost + 5))
            result = "Попадание! Соперник теряет $damage здоровья."
            if (enemyMove == "Подойти" && effectiveDistance == "Ближняя") hp = max(0, hp - if (enemyAction == "Удар ногой") 18 else 12)
        }
        if (enemyHp == 0) {
            wins++; level++; strength += 2; maxHp += 10; hp = min(maxHp, hp + 20)
            result += " Победа! Сила и запас здоровья увеличены."
        }
        if (hp == 0) result += " Поражение. Нажми «Новый бой»."
        status.text = result
        round++
        resolving = false
        arena.invalidate()
        updateStats()
    }

    private fun resetGame() {
        hp = maxHp; enemyHp = 100; stamina = 100; round = 1
        status.text = "Новый бой! Выбери дистанцию, движение и действие."
        arena.invalidate(); updateStats()
    }

    private fun updateStats() {
        stats.text = "ТЫ  $hp/$maxHp HP     CPU  $enemyHp/100 HP     ЭНЕРГИЯ  $stamina\nУРОВЕНЬ $level     СИЛА $strength     ПОБЕДЫ $wins     РАУНД $round"
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    inner class ArenaView : View(this@MainActivity) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat(); val h = height.toFloat()
            canvas.drawColor(Color.rgb(22, 32, 46))
            p.color = Color.rgb(35, 49, 66); p.strokeWidth = 1f
            for (x in 0..w.toInt() step dp(32)) canvas.drawLine(x.toFloat(), 0f, x.toFloat(), h, p)
            for (y in 0..h.toInt() step dp(32)) canvas.drawLine(0f, y.toFloat(), w, y.toFloat(), p)
            p.color = Color.rgb(64, 84, 106); p.strokeWidth = dp(1).toFloat()
            canvas.drawLine(0f, h * .78f, w, h * .78f, p)
            drawFighter(canvas, w * if (move == "Подойти") .44f else if (move == "Отойти") .23f else .32f, h * .72f, Color.rgb(82, 237, 195), true)
            drawFighter(canvas, w * .72f, h * .72f, Color.rgb(255, 101, 127), false)
            p.color = Color.WHITE; p.textSize = dp(10).toFloat()
            canvas.drawText("PLAYER 01", dp(10).toFloat(), dp(18).toFloat(), p)
            p.textAlign = Paint.Align.RIGHT
            canvas.drawText("CPU FIGHTER", w - dp(10).toFloat(), dp(18).toFloat(), p)
            p.textAlign = Paint.Align.LEFT
        }

        private fun drawFighter(c: Canvas, x: Float, base: Float, color: Int, hero: Boolean) {
            p.color = color; p.strokeWidth = dp(5).toFloat(); p.strokeCap = Paint.Cap.ROUND
            c.drawCircle(x, base - dp(68), dp(11).toFloat(), p)
            c.drawLine(x, base - dp(55), x, base - dp(26), p)
            val arm = if (hero) 1 else -1
            val reach = if (hero && action == "Удар рукой") dp(24) else dp(16)
            c.drawLine(x, base - dp(48), x + arm * reach, base - dp(38), p)
            c.drawLine(x, base - dp(48), x - arm * dp(13), base - dp(35), p)
            c.drawLine(x, base - dp(26), x - dp(12), base, p)
            c.drawLine(x, base - dp(26), x + dp(12), base, p)
        }
    }
}
