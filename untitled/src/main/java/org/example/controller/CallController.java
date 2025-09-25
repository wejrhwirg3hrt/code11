package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 通话控制器
 */
@Controller
public class CallController {

    /**
     * 语音通话页面
     */
    @GetMapping("/call/voice")
    public String voiceCall(@RequestParam String roomId,
                           @RequestParam(defaultValue = "false") boolean initiator,
                           Model model) {
        model.addAttribute("roomId", roomId);
        model.addAttribute("callType", "audio");
        model.addAttribute("initiator", initiator);
        return "call/voice";
    }

    /**
     * 视频通话页面
     */
    @GetMapping("/call/video")
    public String videoCall(@RequestParam String roomId,
                           @RequestParam(defaultValue = "false") boolean initiator,
                           Model model) {
        model.addAttribute("roomId", roomId);
        model.addAttribute("callType", "video");
        model.addAttribute("initiator", initiator);
        return "call/video";
    }

    /**
     * 通话首页
     */
    @GetMapping("/call")
    public String callIndex() {
        return "call/index";
    }
}
