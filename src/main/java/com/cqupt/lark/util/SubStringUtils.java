package com.cqupt.lark.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubStringUtils {

    public static String subVideosPath(String fullPath) {
        if (fullPath == null || fullPath.trim().isEmpty()) {
            throw new IllegalArgumentException("路径不能为空");
        }

        // 统一转换为正斜杠处理
        String normalizedPath = fullPath.replace("\\", "/");

        // 查找"videos/"的位置
        int videosIndex = normalizedPath.lastIndexOf("videos/");
        if (videosIndex == -1) {
            throw new IllegalArgumentException("路径中不包含videos目录: " + fullPath);
        }

        // 提取videos/之后的部分
        String relativePath = normalizedPath.substring(videosIndex);

        // 确保路径格式正确
        if (relativePath.split("/").length < 2) {
            throw new IllegalArgumentException("videos目录后缺少文件名: " + fullPath);
        }

        return relativePath;
    }

    public static String[] subCasesStr(String fullPath) {

        String[] lines = fullPath.split("\\r?\\n");

        return Arrays.stream(lines)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

    }

    public static String subCasesUselessPart(String standardStr) {
        int start = standardStr.indexOf('{');
        int end = standardStr.lastIndexOf('}');
        return standardStr.substring(start, end + 1);
    }
}
