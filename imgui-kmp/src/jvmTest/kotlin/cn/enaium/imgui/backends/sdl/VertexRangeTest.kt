package cn.enaium.imgui.backends.sdl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The vertex range of a draw command.
 *
 * A draw list that stays below 64K vertices keeps every `VtxOffset` at zero,
 * a larger one (large meshes, which a backend declares support for) splits the
 * list into blocks and stores the indices relative to the block's base. Both
 * have to resolve to the same absolute vertices, and only those.
 */
class VertexRangeTest {

    @Test
    fun aPlainListReferencesItsOwnVertices() {
        val indices = intArrayOf(0, 1, 2, 2, 1, 3)
        assertEquals(0..3, referencedVertices(indices, 0, indices.size, 0))
    }

    @Test
    fun anOffsetRangeIsRelativeToTheVertexBlock() {
        // Second block of a large mesh: the indices restart at zero while the
        // vertices live at 65532 and beyond.
        val indices = intArrayOf(0, 1, 2, 2, 1, 3)
        assertEquals(65532..65535, referencedVertices(indices, 0, indices.size, 65532))
    }

    @Test
    fun onlyTheReferencedPartOfTheBufferIsCovered() {
        // A command that draws from the middle of the buffer must not report
        // the vertices before it: those belong to the earlier commands and
        // copying them again is what made the backend quadratic.
        val indices = intArrayOf(9, 9, 9, 900, 42, 7, 900)
        assertEquals(7..900, referencedVertices(indices, 3, 4, 0))
    }

    @Test
    fun aCommandWithoutIndicesDrawsNothing() {
        assertNull(referencedVertices(intArrayOf(1, 2, 3), 0, 0, 0))
        assertNull(referencedVertices(intArrayOf(1, 2, 3), 3, 0, 1024))
    }
}
