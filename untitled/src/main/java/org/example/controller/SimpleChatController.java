package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/simple-chat")
public class SimpleChatController {

    @GetMapping
    public String simpleChatPage(Model model) {
        // 添加一些模拟数据
        model.addAttribute("pageTitle", "简单聊天系统");
        model.addAttribute("appName", "视频分享平台");
        
        return "simple-chat";
    }
}
