package com.nawaf.capstone3.Controller;

import com.nawaf.capstone3.Service.WhatsAppService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    public WhatsAppController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @GetMapping("/whatsapp/test")
    public String testWhatsApp() {

        whatsAppService.sendMessage(
                "966595771720",
                "السلام عليكم 👋 هذه رسالة اختبار من Capstone3"
        );

        return "WhatsApp message sent successfully!";
    }
}