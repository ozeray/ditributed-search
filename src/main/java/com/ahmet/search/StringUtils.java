package com.ahmet.search;

import java.util.Arrays;
import java.util.List;

public interface StringUtils {
    static List<String> lineToWords(String line) {
        return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }
}
