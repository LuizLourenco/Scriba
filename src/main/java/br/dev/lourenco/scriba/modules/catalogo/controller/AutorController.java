package br.dev.lourenco.scriba.modules.catalogo.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.modules.catalogo.domain.Autor;
import br.dev.lourenco.scriba.modules.catalogo.dto.AutorForm;
import br.dev.lourenco.scriba.modules.catalogo.service.AutorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/catalogo/autores")
public class AutorController {

    private final AutorService autorService;

    public AutorController(AutorService autorService) {
        this.autorService = autorService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "busca", required = false) String busca, Model model) {
        model.addAttribute("autores", autorService.listarDaInstituicaoAtual(busca));
        model.addAttribute("busca", busca);
        return "catalogo/autores/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("autorForm", new AutorForm());
        return "catalogo/autores/form";
    }

    @PostMapping
    public String criar(
        @Valid AutorForm autorForm,
        BindingResult binding,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            return "catalogo/autores/form";
        }
        try {
            autorService.criar(autorForm);
        } catch (BusinessException exception) {
            binding.reject("autor", exception.getMessage());
            return "catalogo/autores/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Autor criado com sucesso.");
        return "redirect:/catalogo/autores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        Autor autor = autorService.buscarDaInstituicaoAtual(id);
        model.addAttribute("autorId", id);
        model.addAttribute("autorForm", autorService.paraFormulario(autor));
        return "catalogo/autores/form";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable UUID id) {
        throw new ResourceNotFoundException("Autor com id " + id + " não encontrado");
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid AutorForm autorForm,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("autorId", id);
            return "catalogo/autores/form";
        }
        try {
            autorService.atualizar(id, autorForm);
        } catch (BusinessException exception) {
            binding.reject("autor", exception.getMessage());
            model.addAttribute("autorId", id);
            return "catalogo/autores/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Autor atualizado com sucesso.");
        return "redirect:/catalogo/autores";
    }
}
