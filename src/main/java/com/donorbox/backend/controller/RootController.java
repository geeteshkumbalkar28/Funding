package com.donorbox.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class RootController {

    @GetMapping("/")
    public RedirectView redirectToSwagger() {
        return new RedirectView("/swagger-ui.html");
    }
    
    @GetMapping("/health")
    @org.springframework.web.bind.annotation.ResponseBody
    public String health() {
        return "Donorbox Backend API is running!";
    }
}
