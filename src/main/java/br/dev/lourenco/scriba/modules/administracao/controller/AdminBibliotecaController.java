package br.dev.lourenco.scriba.modules.administracao.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.administracao.dto.BibliotecaForm;
import br.dev.lourenco.scriba.modules.administracao.service.BibliotecaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/bibliotecas")
public class AdminBibliotecaController {

    private final BibliotecaService bibliotecaService;

    public AdminBibliotecaController(BibliotecaService bibliotecaService) {
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("bibliotecas", bibliotecaService.listarDaInstituicaoAtual());
        return "admin/bibliotecas/list";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("bibliotecaForm", new BibliotecaForm());
        return "admin/bibliotecas/form";
    }

    @PostMapping
    public String criar(
        @Valid @ModelAttribute("bibliotecaForm") BibliotecaForm form,
        BindingResult binding,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            return "admin/bibliotecas/form";
        }

        try {
            bibliotecaService.criar(form);
        } catch (BusinessException exception) {
            binding.reject("biblioteca", exception.getMessage());
            return "admin/bibliotecas/form";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Biblioteca criada com sucesso.");
        return "redirect:/admin/bibliotecas";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        Biblioteca biblioteca = bibliotecaService.buscarDaInstituicaoAtual(id);
        model.addAttribute("bibliotecaId", id);
        model.addAttribute("bibliotecaForm", bibliotecaService.paraFormulario(biblioteca));
        return "admin/bibliotecas/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid @ModelAttribute("bibliotecaForm") BibliotecaForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("bibliotecaId", id);
            return "admin/bibliotecas/form";
        }

        try {
            bibliotecaService.atualizar(id, form);
        } catch (BusinessException exception) {
            binding.reject("biblioteca", exception.getMessage());
            model.addAttribute("bibliotecaId", id);
            return "admin/bibliotecas/form";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Biblioteca atualizada com sucesso.");
        return "redirect:/admin/bibliotecas";
    }

    @PatchMapping("/{id}/desativar")
    public String desativarPatch(@PathVariable UUID id, Model model) {
        return alterarAtivo(id, false, model);
    }

    @PostMapping("/{id}/desativar")
    public String desativarPost(@PathVariable UUID id, Model model) {
        return alterarAtivo(id, false, model);
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable UUID id, Model model) {
        return alterarAtivo(id, true, model);
    }

    private String alterarAtivo(UUID id, boolean ativo, Model model) {
        bibliotecaService.alterarAtivo(id, ativo);
        model.addAttribute("bibliotecas", bibliotecaService.listarDaInstituicaoAtual());
        return "admin/bibliotecas/list :: tabela";
    }
}
