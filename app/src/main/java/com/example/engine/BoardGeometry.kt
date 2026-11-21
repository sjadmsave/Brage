package com.example.engine

import com.example.model.BoardHole
import com.example.model.Bolt
import com.example.model.LocalPoint
import com.example.model.Plank
import com.example.model.PlankStatus
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

object BoardGeometry {
    const val BOARD_WIDTH = 360f
    const val BOARD_HEIGHT = 520f
    const val DEFAULT_HOLE_RADIUS = 14f
    const val ALIGN_TOLERANCE = 10.5f
    const val BLOCK_MARGIN = 7.5f

    fun localToWorld(plank: Plank, local: LocalPoint): Pair<Float, Float> {
        val c = cos(plank.angleRad)
        val s = sin(plank.angleRad)
        val wx = plank.centerX + local.x * c - local.y * s
        val wy = plank.centerY + local.x * s + local.y * c
        return wx to wy
    }

    fun worldToLocal(plank: Plank, wx: Float, wy: Float): Pair<Float, Float> {
        val dx = wx - plank.centerX
        val dy = wy - plank.centerY
        val c = cos(plank.angleRad)
        val s = sin(plank.angleRad)
        val lx = dx * c + dy * s
        val ly = -dx * s + dy * c
        return lx to ly
    }

    /**
     * Returns the LocalPoint on [plank] aligned with [hole], or null if none aligns within tolerance.
     */
    fun findAlignedLocalHole(plank: Plank, hole: BoardHole): LocalPoint? {
        val (lx, ly) = worldToLocal(plank, hole.x, hole.y)
        var best: LocalPoint? = null
        var bestDist = ALIGN_TOLERANCE
        for (lh in plank.localHoles) {
            val d = hypot(lx - lh.x, ly - lh.y)
            if (d <= bestDist) {
                bestDist = d
                best = lh
            }
        }
        return best
    }

    /**
     * Checks whether [plank] physically overlaps [hole] (without having an aligned plank hole over it).
     */
    fun doesPlankBlockHole(plank: Plank, hole: BoardHole): Boolean {
        if (plank.status == PlankStatus.FALLING || plank.status == PlankStatus.CLEARED) return false

        val (lx, ly) = worldToLocal(plank, hole.x, hole.y)
        val cornerR = minOf(plank.height * 0.45f, 14f)
        val halfW = plank.width * 0.5f - cornerR
        val halfH = plank.height * 0.5f - cornerR
        val qx = max(0f, abs(lx) - halfW)
        val qy = max(0f, abs(ly) - halfH)
        val sdf = hypot(qx, qy) - cornerR

        if (sdf >= BLOCK_MARGIN) {
            return false
        }

        // If the plank has a hole aligned right over this board hole, it doesn't block it
        return findAlignedLocalHole(plank, hole) == null
    }

    /**
     * Returns true if [hole] is empty and no active plank covers/blocks it.
     */
    fun isHoleAvailableForBolt(
        hole: BoardHole,
        bolts: List<Bolt>,
        planks: List<Plank>
    ): Boolean {
        if (bolts.any { it.holeId == hole.id }) return false
        return planks.none { doesPlankBlockHole(it, hole) }
    }

    /**
     * Finds all bolts currently pinning [plank] (as pairs of Bolt and aligned LocalPoint).
     */
    fun getPinningBolts(
        plank: Plank,
        bolts: List<Bolt>,
        holeById: Map<Int, BoardHole>
    ): List<Pair<Bolt, LocalPoint>> {
        if (plank.status == PlankStatus.FALLING || plank.status == PlankStatus.CLEARED) {
            return emptyList()
        }
        val result = ArrayList<Pair<Bolt, LocalPoint>>(4)
        for (bolt in bolts) {
            val hole = holeById[bolt.holeId] ?: continue
            val aligned = findAlignedLocalHole(plank, hole)
            if (aligned != null) {
                result.add(bolt to aligned)
            }
        }
        return result
    }

    /**
     * Computes plank center (cx, cy) given a pivot world point (px, py), its local hole (lx, ly), and angleRad.
     */
    fun centerFromPivot(
        pivotX: Float,
        pivotY: Float,
        localHole: LocalPoint,
        angleRad: Float
    ): Pair<Float, Float> {
        val c = cos(angleRad)
        val s = sin(angleRad)
        val cx = pivotX - (localHole.x * c - localHole.y * s)
        val cy = pivotY - (localHole.x * s + localHole.y * c)
        return cx to cy
    }

    /**
     * Computes the gravity equilibrium angle for a plank pivoting around [localHole].
     * Returns null if pivot is essentially at center of mass.
     */
    fun equilibriumAngleForPivot(localHole: LocalPoint): Float? {
        val vx = -localHole.x
        val vy = -localHole.y
        if (hypot(vx, vy) < 2f) return null
        return (PI / 2.0).toFloat() - atan2(vy, vx)
    }

    fun normalizeAngleDelta(target: Float, current: Float): Float {
        var diff = (target - current) % (2f * PI.toFloat())
        if (diff > PI.toFloat()) diff -= 2f * PI.toFloat()
        if (diff < -PI.toFloat()) diff += 2f * PI.toFloat()
        return diff
    }

    /**
     * Separating Axis Theorem overlap check between two rectangular planks.
     */
    fun obbsOverlap(a: Plank, b: Plank, padding: Float = -3f): Boolean {
        val aHalfW = max(4f, a.width * 0.5f + padding)
        val aHalfH = max(4f, a.height * 0.5f + padding)
        val bHalfW = max(4f, b.width * 0.5f + padding)
        val bHalfH = max(4f, b.height * 0.5f + padding)

        val cosA = cos(a.angleRad)
        val sinA = sin(a.angleRad)
        val cosB = cos(b.angleRad)
        val sinB = sin(b.angleRad)

        val dx = b.centerX - a.centerX
        val dy = b.centerY - a.centerY

        // 4 candidate separating axes: A's X, A's Y, B's X, B's Y
        if (separatedOnAxis(cosA, sinA, dx, dy, cosA, sinA, aHalfW, aHalfH, cosB, sinB, bHalfW, bHalfH)) return false
        if (separatedOnAxis(-sinA, cosA, dx, dy, cosA, sinA, aHalfW, aHalfH, cosB, sinB, bHalfW, bHalfH)) return false
        if (separatedOnAxis(cosB, sinB, dx, dy, cosA, sinA, aHalfW, aHalfH, cosB, sinB, bHalfW, bHalfH)) return false
        if (separatedOnAxis(-sinB, cosB, dx, dy, cosA, sinA, aHalfW, aHalfH, cosB, sinB, bHalfW, bHalfH)) return false

        return true
    }

    private fun separatedOnAxis(
        ax: Float, ay: Float,
        dx: Float, dy: Float,
        cosA: Float, sinA: Float, aHalfW: Float, aHalfH: Float,
        cosB: Float, sinB: Float, bHalfW: Float, bHalfH: Float
    ): Boolean {
        val dist = abs(dx * ax + dy * ay)
        val projA = aHalfW * abs(cosA * ax + sinA * ay) + aHalfH * abs(-sinA * ax + cosA * ay)
        val projB = bHalfW * abs(cosB * ax + sinB * ay) + bHalfH * abs(-sinB * ax + cosB * ay)
        return dist >= projA + projB
    }

    /**
     * Simulates settling a 1-bolt plank to its equilibrium angle, stopping early if it hits a same-layer plank.
     */
    fun settleSingleBoltPlank(
        plank: Plank,
        pivotHole: BoardHole,
        localPivot: LocalPoint,
        otherPlanks: List<Plank>
    ): Plank {
        val eqAngle = equilibriumAngleForPivot(localPivot) ?: return plank.copy(status = PlankStatus.SWINGING)
        var delta = normalizeAngleDelta(eqAngle, plank.angleRad)
        // Break exact upside-down tie deterministically clockwise
        if (abs(abs(delta) - PI.toFloat()) < 0.04f) {
            delta = PI.toFloat() - 0.01f
        }
        val steps = 14
        var current = plank
        val sameLayerOthers = otherPlanks.filter {
            it.id != plank.id &&
                it.layer == plank.layer &&
                it.status != PlankStatus.FALLING &&
                it.status != PlankStatus.CLEARED
        }
        for (step in 1..steps) {
            val t = step.toFloat() / steps
            val nextAngle = plank.angleRad + delta * t
            val (cx, cy) = centerFromPivot(pivotHole.x, pivotHole.y, localPivot, nextAngle)
            val candidate = current.copy(
                centerX = cx,
                centerY = cy,
                angleRad = nextAngle,
                status = PlankStatus.SWINGING,
                angularVelocity = 0f
            )
            val collided = sameLayerOthers.any { obbsOverlap(candidate, it, padding = -2.5f) }
            if (collided) {
                break
            }
            current = candidate
        }
        return current
    }
}
