package finalProject.service.account;

import finalProject.domain.AuthInfoDTO;
import finalProject.mapper.AccountMapper;
import finalProject.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountPointChargeService {
    @Autowired
    AccountMapper accountMapper;

    @Autowired
    UserMapper userMapper;

    public void execute(Integer point, HttpSession session) {
        AuthInfoDTO auth = (AuthInfoDTO) session.getAttribute("auth");
        String userNum = userMapper.getUserNumById(auth.getUserId());
        if(accountMapper.checkPoint(userNum) != null) {
            accountMapper.pointUpdate(point, userNum);
        } else{
            accountMapper.pointCharge(point, userNum);
        }
    }
}