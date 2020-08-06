package com.dhwingert.fedcom.hexgridmap;

/**
 * Created by David Wingert on 3/4/2015.
 */
public class HexGridMapDefinition {

    // Basic info taken from:  http://www.redblobgames.com/grids/hexagons/

    // HEX DIMENSIONS FOR DRAWING ON SCREEN
    //
    //      | 1 | 2 | 3 | 4 |  = 4x Width Base Size
    //  -       *---*---*      -
    //        /           \
    //  1   /              \   1
    //  -  *    |   |   |   *  -  = 2x Hex Height Base Size
    //      \              /
    //  2     \          /     2
    //  -       *---*---*      -
    //      | 1 | 2 | 3 | 4 |
    //
    // Height Base Size is approx = 1.8 * Width Base Size
    //
    //      A hex with a flat top will be 4x Width Base Size this from point to point.
    //      The top and bottom will be 2x tWidth Base Height.
    //
    //      Height of any one diagonal side is 1x Height Base Size
    //      Distance from flat top to flat bottom is 2x Height Base Size

    private final long mHexWidthBaseSize = 20; //32;
    private final long mHexHeightBaseSize = (long) (1.8 * mHexWidthBaseSize);

    public long getHexWidthBaseSize() {
        return mHexWidthBaseSize;
    }

    public long getHexHeightBaseSize() {
        return mHexHeightBaseSize;
    }

    // Return the Y position of the center of the hex this coordinate represents on the map
    public long getHexCenterOffsetY(HexGridPosition iconGridPos) {

        // First calculate top of hex this coordinate represents (grid coordinates are 0 based)
        //      Hexes are 2x Base Height
        long iconPosOffsetY = (iconGridPos.getSquareGridPosY() * 2 * this.getHexHeightBaseSize());

        // Center of hex is 1x Base Height from top
        iconPosOffsetY += this.getHexHeightBaseSize();

        // Odd columns are offset half a hex (1x Base Height) down
        if (iconGridPos.getSquareGridPosX() % 2 == 1) {
            iconPosOffsetY += this.getHexHeightBaseSize();
        }

        return iconPosOffsetY;
    }

    // Return the X position of the center of the hex this coordinate represents on the map
    public long getHexCenterOffsetX(HexGridPosition iconGridPos) {

        // First calculate the left edge of the hex this coordinate represents on the map
        //      Hexes are effectively 3x Base Width because a column of hexes shares
        //      the space for the diagonals with the column after it
        long iconPosOffsetX = (iconGridPos.getSquareGridPosX() * 3 * this.getHexWidthBaseSize());

        // Center of the hex is 1x Base Width from the left edge
        //      NOTE: First column of hexes (on left edge of map) has its diagonals cut off.
        //            The first column has its square center and right edge diagonals.
        iconPosOffsetX += this.getHexWidthBaseSize();

        return iconPosOffsetX;
    }

}
