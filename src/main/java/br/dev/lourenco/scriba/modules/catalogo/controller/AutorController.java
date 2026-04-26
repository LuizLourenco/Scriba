package br.dev.lourenco.scriba.modules.catalogo.controller;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/catalogo/autores")
public class AutorController {

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("autores", List.of("Machado de Assis", "Carolina Maria de Jesus"));
        return "catalogo/autores";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable UUID id) {
        throw new ResourceNotFoundException("Autor com id " + id + " não encontrado");
    }
}
