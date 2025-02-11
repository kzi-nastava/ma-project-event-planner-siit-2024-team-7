package rs.ac.uns.eventplanner.team7.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> {
    private List<T> content = new ArrayList<>();
    private Pageable pageable = Pageable.getDefault();
    private int totalElements = 0;
    private int totalPages = 1;
    private boolean first = true;
    private boolean last = true;
    private boolean empty = true;

    public Page(List<T> content, Pageable pageable) {
        this.content = content;
        this.pageable = pageable;
        totalElements = content.size();
        empty = content.isEmpty();
    }

    public void update(Page<T> other) {
        content = new ArrayList<>(other.content);
        pageable = other.pageable;
        totalElements = other.totalElements;
        first = other.first;
        last = other.last;
        empty = other.empty;
    }

    public void resetToDefault() {
        update(getDefault());
    }

    public Map<String, String> toQueryMap() {
        return pageable.toQueryMap();
    }

    public int getPageNumber() {
        return pageable.getPageNumber();
    }

    /**
     * Increments current page, if it is not last
     */
    public void nextPage() {
        if (last) return;
        pageable.setPageNumber(getPageNumber()+1);
    }

    public void setSort(Sort sort) {
        pageable.setSort(sort);
    }

    public static <T> Page<T> getDefault() {
        return new Page<>(new ArrayList<>(), Pageable.getDefault(),
                0, 1, true, true, true);
    }
}
