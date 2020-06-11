/*
 *
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.util;

import com.terraforged.n2d.util.NoiseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListUtils {

    public static <T> T get(List<T> list, float value, T def) {
        if (list.isEmpty()) {
            return def;
        }
        return get(list, list.size() - 1, value, def);
    }

    public static <T> T get(List<T> list, int maxIndex, float value, T def) {
        int index = NoiseUtil.round(value * maxIndex);
        if (index < list.size()) {
            return list.get(index);
        }
        return def;
    }

    public static <T> List<T> minimize(List<T> list) {
        Map<T, Integer> counts = count(list);
        List<T> result = new ArrayList<>(list.size());
        int min = counts.values().stream().min(Integer::compareTo).orElse(1);
        for (T t : list) {
            int count = counts.get(t);
            int amount = count / min;
            for (int i = 0; i < amount; i++) {
                result.add(t);
            }
        }
        return result;
    }

    public static <T> Map<T, Integer> count(List<T> list) {
        Map<T, Integer> map = new HashMap<>(list.size());
        for (T t : list) {
            int count = map.getOrDefault(t, 0);
            map.put(t, ++count);
        }
        return map;
    }
}
