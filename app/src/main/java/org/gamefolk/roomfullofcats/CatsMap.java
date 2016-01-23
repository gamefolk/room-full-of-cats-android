package org.gamefolk.roomfullofcats;

import java.util.Arrays;

public class CatsMap {
    private Cat[][] map;

    public CatsMap(int width, int height) {
        if (width == 0) {
            throw new IllegalArgumentException("Width cannot be 0.");
        }

        if (height == 0) {
            throw new IllegalArgumentException("Height cannot be 0.");
        }

        this.map = new Cat[width][height];
    }

    public int getWidth() {
        return map.length;
    }

    public int getHeight() {
        return map[0].length;
    }

    public void setCat(int col, int row, Cat cat) {
        map[col][row] = cat;
    }

    public Cat getCat(int col, int row) {
        return map[col][row];
    }

    /**
     * Empties the map.
     */
    public void clear() {
        for (Cat[] row : map) {
            Arrays.fill(row, null);
        }
    }
}
