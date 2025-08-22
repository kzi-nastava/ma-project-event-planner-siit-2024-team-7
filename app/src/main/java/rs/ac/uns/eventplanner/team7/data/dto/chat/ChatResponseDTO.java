package rs.ac.uns.eventplanner.team7.data.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private Integer id;
    private Integer senderId;
    private String senderEmail;
    private String senderPhotoUrl;
    private String recipientEmail;
    private String message;
    private String timestamp;
    private boolean read;
}
