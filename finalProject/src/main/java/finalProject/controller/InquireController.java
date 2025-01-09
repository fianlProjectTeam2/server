package finalProject.controller;

import finalProject.command.InquireCommand;
import finalProject.domain.AuthInfoDTO;
import finalProject.service.AutoNumService;
import finalProject.service.inquire.InquireListService;
import finalProject.service.inquire.InquireWriteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequestMapping("inquire")
public class InquireController {
    @Autowired
    InquireListService inquireListService;
    @Autowired
    AutoNumService autoNumService;
    @Autowired
    InquireWriteService inquireWriteService;

    @GetMapping("inquireList")
    public String postList(@RequestParam(value = "searchWord", required = false) String searchWord,
                           @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                           Model model) {
        inquireListService.execute(page,searchWord,model);
        return "thymeleaf/inquire/inquireList";
    }

    @GetMapping("inquireWrite")
    public String inquireWrite(Model model, HttpSession session){
        AuthInfoDTO auth = (AuthInfoDTO)session.getAttribute("auth");
    if(auth == null){
        try{
            String message= URLEncoder.encode("로그인이 필요합니다.", "UTF-8");
            return "redirct:/login?meggsage=" + message;
        } catch(UnsupportedEncodingException e){
            e.printStackTrace();
            return "redirect:/login";
        }


    }
    String autoNum=autoNumService.execute("inquire_", "inquire_num",6,"inquire");
    InquireCommand inquireCommand = new InquireCommand();
    inquireCommand.setInquireNum(autoNum);
    model.addAttribute("inquireCommand", inquireCommand);
    return "thymeleaf/inquire/inquireForm";

    }
    @PostMapping("inquireRegist")
    public String inquireRegist(InquireCommand inquireCommand, HttpSession session){
        inquireWriteService.execute(inquireCommand, session);
        return "redirect:/inquire/inquireList";

    }
}

