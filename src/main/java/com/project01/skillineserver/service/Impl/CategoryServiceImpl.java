package com.project01.skillineserver.service.Impl;

import com.project01.skillineserver.dto.reponse.CategoryResponse;
import com.project01.skillineserver.dto.reponse.PageResponse;
import com.project01.skillineserver.dto.request.CategoryReq;
import com.project01.skillineserver.entity.CategoryEntity;
import com.project01.skillineserver.enums.ErrorCode;
import com.project01.skillineserver.excepion.CustomException.AppException;
import com.project01.skillineserver.mapper.CategoryMapper;
import com.project01.skillineserver.repository.CategoryRepository;
import com.project01.skillineserver.service.CategoryService;
import com.project01.skillineserver.utils.MapUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = AppException.class)
    public void save(CategoryReq category) throws IOException {
        CategoryEntity categoryInDB = Optional.ofNullable(category.id())
                .flatMap(categoryRepository::findById)
                .orElse(new CategoryEntity());

        categoryInDB.setName(category.name());
        categoryInDB.setActive(true);
        categoryInDB.setSlug(category.slug());

        categoryRepository.save(categoryInDB);
    }

    @Override
    public PageResponse<CategoryResponse> getCategoryPagination(int page, int size, String sort, String keyword) {
        Sort sortField = MapUtil.parseSort(sort);
        PageRequest pageRequest  = PageRequest.of(page-1, size,sortField);

        Page<CategoryEntity> pageCategories = categoryRepository.getCategories(keyword,pageRequest);

        List<CategoryResponse> list = pageCategories.getContent().stream().map(categoryMapper::toCategoriesResponse).toList();

        return PageResponse.<CategoryResponse>builder()
                .list(list)
                .page(page)
                .size(size)
                .totalElements(pageCategories.getTotalElements())
                .totalPages(pageCategories.getTotalPages())
                .build();
    }

    @Override
    public PageResponse<CategoryResponse> getCategoryMySelf(int page, int size, String sort, String keyword, Long userId) {
        Sort sortField = MapUtil.parseSort(sort);
        PageRequest pageRequest  = PageRequest.of(page-1, size,sortField);

        Page<CategoryEntity> pageCategories = categoryRepository.getCategoriesMySelf(keyword,userId,pageRequest);

        List<CategoryResponse> list = pageCategories.getContent().stream().map(categoryMapper::toCategoriesResponse).toList();

        return PageResponse.<CategoryResponse>builder()
                .list(list)
                .page(page)
                .size(size)
                .totalElements(pageCategories.getTotalElements())
                .totalPages(pageCategories.getTotalPages())
                .build();
    }

    @Override
    @Transactional(rollbackFor = {AppException.class})
    public void delete(List<Long> categoryIds) {
        if(categoryIds == null || categoryIds.isEmpty()){
            throw new AppException(ErrorCode.LIST_ID_EMPTY);
        }

        categoryRepository.deleteCategoryByIds(categoryIds);
    }

}
