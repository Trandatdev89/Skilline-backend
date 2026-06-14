package com.project01.skillineserver.utils;

import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MapUtil {
    public static <X,Y> Map<X,Y> extractInfo(Object dataNeedExtract) throws IllegalAccessException {

        if(Objects.isNull(dataNeedExtract)){
            throw new AppException(ErrorCode.INTERNAL_SERVER);
        }

        Map<X,Y> infoExtract = new HashMap<>();
        Field[] fields = dataNeedExtract.getClass().getDeclaredFields();
        for (Field field : fields){
            field.setAccessible(true);
            String key = field.getName();
            Object value = field.get(dataNeedExtract);
            if (value == null) continue;

            if (!(value instanceof String) || StringUtils.hasText((String) value)) {
                infoExtract.put((X) key, (Y) value);
            }
        }
        return infoExtract;
    }
    public static Sort parseSort(String sort){
        if(!StringUtils.hasText(sort)){
            return Sort.by(Sort.Direction.DESC,"createdAt");
        }

        String[] parts = sort.split(":");

        String directionSort = parts[1];
        Sort.Direction direction = directionSort.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction,parts[0]);

    }

}
