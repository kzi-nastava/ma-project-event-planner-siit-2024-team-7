package rs.ac.uns.eventplanner.team7.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pageable {
    private int pageNumber = 0;
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

    public Map<String, String> toQueryMapWithSort() {
        return Map.of("page", String.valueOf(pageNumber),
                "size", String.valueOf(pageSize),
                "sort", String.format("%s,%s", sort.getBy(), sort.getDirection()));
    }

    public Map<String, String> toQueryMap() {
        return Map.of("page", String.valueOf(pageNumber),
                "size", String.valueOf(pageSize));
    }

    public void resetToDefault() {
        Pageable defaultPageable = getDefault();
        pageNumber = defaultPageable.getPageNumber();
        pageSize = defaultPageable.getPageSize();
        sort.resetToDefault();
    }
}
