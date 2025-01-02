package rs.ac.uns.eventplanner.team7.dto;

import android.annotation.SuppressLint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Pageable {
    @Setter private int pageNumber = 0;
    private int pageSize = 10;
    private Sort sort = Sort.getDefault();

    public Pageable(int pageNumber, Sort sort) {
        if (pageNumber < 0) throw new IllegalArgumentException("Page number must be non negative!");
        this.pageNumber = pageNumber;
        this.sort = sort;
    }

    public static Pageable getDefault() {
        return new Pageable(0, 10, Sort.getDefault());
    }

    @SuppressLint("DefaultLocale")
    public String toQuery() {
        return String.format("page=%d&size=10&sort=%s,%s", pageNumber, sort.getBy(), sort.getDirection());
    }

    public void resetToDefault() {
        Pageable defaultPageable = getDefault();
        pageNumber = defaultPageable.getPageNumber();
        pageSize = defaultPageable.getPageSize();
        sort.resetToDefault();
    }
}
