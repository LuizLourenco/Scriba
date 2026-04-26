package br.dev.lourenco.scriba.modules.catalogo.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.catalogo.domain.Editora;
import br.dev.lourenco.scriba.modules.catalogo.dto.EditoraForm;
import br.dev.lourenco.scriba.modules.catalogo.service.EditoraService;
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
@RequestMapping("/catalogo/editoras")
public class EditoraController {

    private final EditoraService editoraService;

    public EditoraController(EditoraService editoraService) {
        this.editoraService = editoraService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "busca", required = false) String busca, Model model) {
        model.addAttribute("editoras", editoraService.listarDaInstituicaoAtual(busca));
        model.addAttribute("busca", busca);
        return "catalogo/editoras/list";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("editoraForm", new EditoraForm());
        return "catalogo/editoras/form";
    }

    @PostMapping
    public String criar(@Valid EditoraForm editoraForm, BindingResult binding, RedirectAttributes redirectAttributes) {
        if (binding.hasErrors()) {
            return "catalogo/editoras/form";
        }
        try {
            editoraService.criar(editoraForm);
        } catch (BusinessException exception) {
            binding.reject("editora", exception.getMessage());
            return "catalogo/editoras/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Editora criada com sucesso.");
        return "redirect:/catalogo/editoras";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        Editora editora = editoraService.buscarDaInstituicaoAtual(id);
        model.addAttribute("editoraId", id);
        model.addAttribute("editoraForm", editoraService.paraFormulario(editora));
        return "catalogo/editoras/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid EditoraForm editoraForm,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("editoraId", id);
            return "catalogo/editoras/form";
        }
        try {
            editoraService.atualizar(id, editoraForm);
        } catch (BusinessException exception) {
            binding.reject("editora", exception.getMessage());
            model.addAttribute("editoraId", id);
            return "catalogo/editoras/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Editora atualizada com sucesso.");
        return "redirect:/catalogo/editoras";
    }
}
