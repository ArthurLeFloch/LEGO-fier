package com.alf.legofy

import android.graphics.Color

class Pixel(_enabled: Boolean, _r:Int, _g:Int, _b:Int) {
    companion object{
        val lego_colors = listOf(Triple(254, 205, 6),   // Bright Yellow
            Triple(255, 245, 121),                      // Cool Yellow
            Triple(245, 125, 32),                       // Bright Orange
            Triple(251, 171, 24),                       // Flame Yellowish Orange
            Triple(221, 26, 33),                        // Bright Red
            Triple(233, 93, 162),                       // Bright Purple
            Triple(246, 173, 205),                      // Light Purple
            Triple(182, 28, 126),                       // Bright Reddish Violet
            Triple(126, 19, 27),                        // Dark Red
            Triple(149, 118, 178),                      // Medium Lavender
            Triple(188, 165, 207),                      // Lavender
            Triple(76, 47, 146),                        // Medium Lilac
            Triple(2, 108, 184),                        // Bright Blue
            Triple(72, 158, 206),                       // Medium Blue
            Triple(103, 130, 151),                      // Sand Blue
            Triple(1, 57, 94),                          // Earth Blue
            Triple(0, 163, 218),                        // Dark Azur
            Triple(0, 190, 212),                        // Medium Azur
            Triple(193, 228, 218),                      // Aqua
            Triple(0, 175, 80),                         // Bright Green
            Triple(112, 148, 122),                      // Sand Green
            Triple(1, 146, 71),                         // Dark Green
            Triple(0, 75, 45),                          // Earth Green
            Triple(156, 201, 59),                       // Bright Yellowish Green
            Triple(130, 131, 83),                       // Olive Green
            Triple(106, 46, 20),                        // Reddish Brown
            Triple(221, 196, 142),                      // Brick Yellow
            Triple(149, 125, 97),                       // Sand Yellow
            Triple(173, 116, 71),                       // Medium Nougat
            Triple(59, 25, 16),                         // Dark Brown
            Triple(253, 195, 158),                      // Light Nougat
            Triple(166, 84, 36),                        // Dark Orange
            Triple(255,255,255),                        // White
            Triple(160,160,160),                        // Medium Stone Grey
            Triple(103,103,103),                        // Dark Stone Grey
            Triple(0,0,0),                              // Black
            Triple(195, 151, 56),                       // Warm Gold
            Triple(135, 140, 143),                      // Silver Metallic
            Triple(24, 158, 159),                       // Bright Bluish Green
            Triple(249, 108, 98))                       // Vibrant Coral
    }

    val r = _r
    val g = _g
    val b = _b
    val color = Color.rgb(r, g, b)
    var enabled = _enabled
    val colorCode = findColorId(_r, _g, _b)

    private fun findColorId(r: Int, g: Int, b: Int): Int{
        for(k in lego_colors.indices){
            val c = lego_colors[k]
            if(c.first == r && c.second == g && c.third == b){
                return k
            }
        }
        return -1
    }
}