package rs.ac.uns.eventplanner.team7.data.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.WithImage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatContactDTO implements BasicCard, WithImage {
    private Integer userId;
    private String userEmail;
    private String photoUrl;
    private boolean read;

    @Override
    public Integer getId() {
        return userId;
    }

    @Override
    public String getTitle() {
        return userEmail;
    }

    @Override
    public String getSubtitle() {
        return "";
    }

    @Override
    public String getCoverImage() {
        return photoUrl;
    }
}
