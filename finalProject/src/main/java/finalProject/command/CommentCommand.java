package finalProject.command;

import lombok.Data;

import java.util.Date;

@Data
public class CommentCommand {
    String commentNum;        // 댓글 번호
    String commentAuthor;     // 댓글 작성자
    String postAuthor;        // 게시글 작성자
    String postNum;           // 게시글 번호
    String commentContents;   // 댓글 내용
    Date commentTime;         // 댓글 작성 시간
}
