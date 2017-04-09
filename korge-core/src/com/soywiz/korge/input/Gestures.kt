package com.soywiz.korge.input

import com.soywiz.korge.component.Component
import com.soywiz.korge.view.View
import com.soywiz.korio.async.Signal
import com.soywiz.korma.geom.IPoint

class Gestures(view: View) : Component(view) {
	class Direction(val point: IPoint) {
		constructor(x: Int, y: Int) : this(IPoint(x, y))

		val x get() = point.x
		val y get() = point.y

		companion object {
			val Up = Direction(0, -1)
			val Down = Direction(0, +1)
			val Left = Direction(-1, 0)
			val Right = Direction(+1, 0)
		}
	}

	val onSwipe = Signal<Direction>()
}

val View.gestures get() = this.getOrCreateComponent { com.soywiz.korge.input.Gestures(this) }
