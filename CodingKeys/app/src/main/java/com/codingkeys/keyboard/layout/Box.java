/*
 * Coding Keys - an Android keyboard for programming.
 * Copyright (C) 2026 Coding Keys contributors
 * Derived from CodeBoard (https://github.com/gazlaws-dev/codeboard),
 * Copyright (C) 2020 Gazala Muhamed, licensed under GNU GPL-3.0-or-later.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.codingkeys.keyboard.layout;

/**
 * A rectangle in normalized keyboard coordinate space (0..1).
 * Derived from CodeBoard's Box class.
 */
public class Box {

    public final float x;
    public final float y;
    public final float width;
    public final float height;

    private Box(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static Box create(float x, float y, float width, float height) {
        return new Box(x, y, width, height);
    }

    public float getLeft() {
        return x;
    }

    public float getRight() {
        return x + width;
    }

    public float getTop() {
        return y;
    }

    public float getBottom() {
        return y + height;
    }
}
