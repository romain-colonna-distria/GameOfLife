package fr.rom.gameoflife.utils;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Coordinate implements Serializable {
    @Serial
    private static final long serialVersionUID = 2405172041950251807L;

    @Getter @Setter
    private int x;
    @Getter @Setter
    private int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isAdjacent(final Coordinate o) {
        return x == o.x -1 || x == o.x || x == o.x + 1
                && y == o.y -1 || y == o.y || y == o.y + 1;
    }

    public static Set<Coordinate> getAdjacentCoordinate(final Coordinate o) {
        final Set<Coordinate> adjacent = new HashSet<>();
        for(int i = o.x - 1; i <= o.x + 1; i++) {
            for(int j = o.y - 1; j <= o.y + 1; j++) {
                if(i == o.x && j == o.y) continue;
                adjacent.add(new Coordinate(i, j));
            }
        }
        return adjacent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
