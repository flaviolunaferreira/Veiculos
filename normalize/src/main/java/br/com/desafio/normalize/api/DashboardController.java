package br.com.desafio.normalize.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    @GetMapping("/")
    public String dashboard(Model model) {
        return "dashboard";
    }
}

