package rs.ac.uns.eventplanner.team7.utils;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.LocalDate;

public class CurrentDayDecorator implements DayViewDecorator {
    private final int color;
    private final CalendarDay day;

    public CurrentDayDecorator(LocalDate date, int color) {
        this.color = color;
        this.day = CalendarDay.from(date);
    }
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if (this.day.equals(day)){
            return true;
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(3,color));
    }
}