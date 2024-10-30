package com.sofm.recommend.common.utils;

import java.nio.MappedByteBuffer;
import java.util.Objects;

public class MemoryUtils {

    // 使用反射清理 MappedByteBuffer，确保释放堆外内存
    public static void unmap(MappedByteBuffer buffer) {
        try {
            if (Objects.nonNull(buffer)) {
                buffer.force();
            }
        } catch (Exception e) {
            System.err.println("Failed to unmap MappedByteBuffer: " + e.getMessage());
        }
    }
}
