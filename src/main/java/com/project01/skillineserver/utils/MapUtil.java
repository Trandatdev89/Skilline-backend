package com.project01.skillineserver.utils;

import com.project01.skillineserver.entity.MediaAssetEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.properties.CdnProperties;
import com.project01.skillineserver.repository.MediaAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MapUtil {

    private final MediaAssetRepository mediaAssetRepository;
    private final CdnProperties cdnProperties;

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

    public <T, R> List<R> handleComputedThumbnail(List<T> data, Function<T, String> assetIdExtractor,
                                                  BiFunction<T, String, R> mapper) {

        Set<String> assetIds = data.stream()
                .map(assetIdExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> thumbnailUrlByAssetId = mediaAssetRepository
                .findAllByIdIn(assetIds)
                .stream()
                .collect(Collectors.toMap(
                        MediaAssetEntity::getId,
                        asset -> cdnProperties.getDomain() + "/" + asset.getObjectKey()
                ));

        return data.stream()
                .map(entity -> mapper.apply(entity, thumbnailUrlByAssetId.get(assetIdExtractor.apply(entity))))
                .toList();
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
