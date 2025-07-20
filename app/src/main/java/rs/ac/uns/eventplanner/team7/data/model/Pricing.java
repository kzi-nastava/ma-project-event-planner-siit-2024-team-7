package rs.ac.uns.eventplanner.team7.data.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pricing {
    private Integer id;
    private double price;
    private double discount;
    private LocalDate activeFrom;
    private boolean isDeleted;

    public double getDiscountedPrice() {
        return price * (1-discount);
    }
}