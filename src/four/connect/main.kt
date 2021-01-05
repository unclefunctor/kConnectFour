/**
 * Almas Baimagambetov's YouTube Connect Four vid ported to Kotlin:
 *
 *    https://www.youtube.com/watch?v=B5H_t0A_C14
 */
package four.connect

import four.connect.PlayerColor.RED
import four.connect.PlayerColor.YELLOW
import javafx.animation.TranslateTransition
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.effect.Light.Distant
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.stage.Stage
import javafx.scene.effect.Lighting
import javafx.util.Duration

fun main(args: Array<String>) = Application.launch(Main::class.java, *args)

const val COLS = 7
const val ROWS = 6
const val TILE_SIZE = 80.0 // The double type keeps Kotlin happy

enum class PlayerColor(val rgb: Color) { RED(Color.RED), YELLOW(Color.YELLOW) }

class Disc(val color: PlayerColor) : Circle(TILE_SIZE / 2, color.rgb) {
	init {
		centerX = TILE_SIZE / 2
		centerY = TILE_SIZE / 2
	}
}

class Pt(val x: Int, val y: Int) // JavaFX's Point2D class uses inconvenient doubles

var player: PlayerColor = RED
var discs: Array<Array<Disc?>> = Array(COLS) { Array(ROWS) { null } }
val discRoot = Pane()

class Main : Application() {
	override fun start(primaryStage: Stage) {
		primaryStage
			.apply {
				title = "Connect Four"
				scene = Scene(createContent())
			}
			.show()
	}

	private fun createContent(): Parent {
		return Pane().apply {
			children.add(discRoot)
			children.add(makeGrid())
			children.addAll(makeColumns())
		}
	}

	private fun makeGrid(): Shape {
		var frame: Shape = Rectangle((COLS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE)

		val circle = Circle(TILE_SIZE / 2)
		for (y in 0 until ROWS) {
			for (x in 0 until COLS) {
				frame = Shape.subtract(frame, circle.apply {
					translateX = x * (TILE_SIZE + 5) + TILE_SIZE / 2 + TILE_SIZE / 4
					translateY = y * (TILE_SIZE + 5) + TILE_SIZE / 2 + TILE_SIZE / 4
				})
			}
		}

		frame.fill = Color.BLUE
		frame.effect = Lighting().apply {
			light = Distant().apply {
				azimuth = 45.0
				elevation = 30.0
			}
			surfaceScale = 5.0
		}

		return frame
	}

	private fun makeColumns(): List<Rectangle> {
		return (0 until COLS).map { x ->
			Rectangle(TILE_SIZE, (ROWS + 1) * TILE_SIZE).apply {
				translateX = translateCell(x)
				fill = Color.TRANSPARENT
				setOnMouseEntered { fill = Color.rgb(200, 200, 50, 0.3) }
				setOnMouseExited { fill = Color.TRANSPARENT }
				setOnMouseClicked { placeDisc(Disc(player), x) }
			}
		}
	}

	private fun placeDisc(disc: Disc, col: Int) {
		for (row in ROWS - 1 downTo 0) {
			if (discs[col][row] == null) {
				discs[col][row] = disc
				discRoot.children.add(disc)

				disc.translateX = translateCell(col)
				TranslateTransition(Duration.seconds(0.5), disc)
					.apply {
						toY = translateCell(row)
						setOnFinished {  // Cycle the game logic when the drop is done
							if (isGameEnded(col, row)) gameOver()
							player = if (player == RED) YELLOW else RED
						}
					}
					.play()
				break
			}
		}
	}

	private fun isGameEnded(col: Int, row: Int): Boolean {
		if (checkRange((row - 3..row + 3).map { r -> Pt(col, r) })) return true // Vertical
		if (checkRange((col - 3..col + 3).map { c -> Pt(c, row) })) return true // Horizontal
		if (checkRange((col - 3..col + 3).map { c -> Pt(c, row - (col - c)) })) return true // Up Diag
		if (checkRange((col - 3..col + 3).map { c -> Pt(c, row - (c - col)) })) return true // Down Diag
		return false
	}

	private fun checkRange(pts: List<Pt>): Boolean {
		var chain = 0
		pts.forEach {
			if (player == getDisc(it)?.color) {
				if (++chain == 4) return true
			} else {
				chain = 0
			}
		}
		return false
	}

	private fun translateCell(index: Int) = index * (TILE_SIZE + 5) + TILE_SIZE / 4

	private fun getDisc(pt: Pt) = if (pt.x in 0 until COLS && pt.y in 0 until ROWS) discs[pt.x][pt.y] else null

	private fun gameOver() {
		println("$player won!")
		Platform.exit()
	}

}