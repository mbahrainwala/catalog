package com.catalog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    
    @RequestMapping(value = {"/"})
    public String index() {
        return "forward:/index.html";
    }
    
    @RequestMapping(value = {"/admin", "/login"})
    public String spa() {
        return "forward:/index.html";
    }
}