package finalProject.mapper;

import finalProject.domain.InquireDTO;
import finalProject.domain.MemberDTO;
import finalProject.domain.StartEndPageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface InquireMapper {
    List<InquireDTO> inquireSelectList(@Param("sepDTO") StartEndPageDTO sepDTO,
                                       @Param("memberNum") String memberNum);
    public int inquireCount(@Param("sepDTO") StartEndPageDTO sepDTO,
                            @Param("memberNum") String memberNum);
    public InquireDTO inquireSelectOne(String inquireNum);
    MemberDTO getMemberInfo(String userId);
    MemberDTO getMemberInfoByNum(String memberNum);
    public void insertInquire(InquireDTO inquireDTO);
    public String findAuthorldByInquireNum(String inquireNum);
    public void inquireUpdate(InquireDTO dto);
    public void deleteAnswerByInquireNum(String inquireNum);
    public void deleteInquireByInquireNum(String inquireNum);
}
