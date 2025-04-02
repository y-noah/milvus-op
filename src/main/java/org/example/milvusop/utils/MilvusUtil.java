package org.example.milvusop.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MilvusUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将 `String archiveFeature` 转换为 `List<Float>`
     */
    public static List<Float> arcsoftToFloat(String feature) {
        if (feature == null || feature.trim().isEmpty()) {
            return List.of(); // 返回空列表，避免空指针
        }

        // ① 如果是 JSON 数组格式，如 "[0.12, 0.34, -0.56]"
        if (feature.startsWith("[") && feature.endsWith("]")) {
            return parseJsonFeature(feature);
        }

        // ② 如果是逗号分隔的字符串，如 "0.12,0.34,-0.56"
        if (feature.contains(",")) {
            return parseCommaSeparatedFeature(feature);
        }

        throw new IllegalArgumentException("Unsupported feature format: " + feature);
    }

    // 解析 JSON 字符串
    private static List<Float> parseJsonFeature(String feature) {
        try {
            return objectMapper.readValue(feature, new TypeReference<List<Float>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON feature: " + feature, e);
        }
    }

    // 解析逗号分隔的字符串
    private static List<Float> parseCommaSeparatedFeature(String feature) {
        List<Float> floatList = new ArrayList<>();
        try {
            String[] parts = feature.split(",");
            for (String part : parts) {
                floatList.add(Float.parseFloat(part.trim()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse comma-separated feature: " + feature, e);
        }
        return floatList;
    }
}
