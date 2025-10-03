package br.com.gabryel.waes.rdc.banking.controller.dto;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface PageDto<T> {

    static <T> PageDto<T> of(Page<T> page) {
        return new SpringPageWrapperDto<T>(page);
    }

    <V> PageDto<V> map(Function<T, V> mapper);
    List<T> getContent();
    Integer getPageNumber();
    Integer getPageSize();
    Integer getTotalElements();
}


@AllArgsConstructor
class SpringPageWrapperDto<T> implements PageDto<T> {

    private final Page<T> page;

    @Override
    public <V> PageDto<V> map(Function<T, V> mapper) {
        return new SpringPageWrapperDto<>(page.map(mapper));
    }

    @Override
    public List<T> getContent() {
        return page.getContent();
    }

    @Override
    public Integer getPageNumber() {
        return page.getNumber();
    }

    @Override
    public Integer getPageSize() {
        return page.getSize();
    }

    @Override
    public Integer getTotalElements() {
        return page.getTotalPages();
    }
}