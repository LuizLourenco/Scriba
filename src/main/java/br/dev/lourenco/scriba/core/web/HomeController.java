package br.dev.lourenco.scriba.core.web;

import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import br.dev.lourenco.scriba.core.security.annotation.CurrentUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@CurrentUser Usuario usuario, Model model) {
        model.addAttribute("usuario", usuario);
        return "home";
    }

    @GetMapping("/login")
    public String login(@RequestParam(name = "error", defaultValue = "false") boolean error, Model model) {
        model.addAttribute("error", error);
        return "login";
    }
}
