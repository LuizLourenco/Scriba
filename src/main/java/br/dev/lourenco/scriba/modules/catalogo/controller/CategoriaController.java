package br.dev.lourenco.scriba.modules.catalogo.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.catalogo.domain.Categoria;
import br.dev.lourenco.scriba.modules.catalogo.dto.CategoriaForm;
import br.dev.lourenco.scriba.modules.catalogo.service.CategoriaService;
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
@RequestMapping("/catalogo/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "busca", required = false) String busca, Model model) {
        model.addAttribute("categorias", categoriaService.listarDaInstituicaoAtual(busca));
        model.addAttribute("busca", busca);
        return "catalogo/categorias/list";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        prepararFormulario(model, new CategoriaForm());
        return "catalogo/categorias/form";
    }

    @PostMapping
    public String criar(
        @Valid CategoriaForm categoriaForm,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            prepararFormulario(model, categoriaForm);
            return "catalogo/categorias/form";
        }
        try {
            categoriaService.criar(categoriaForm);
        } catch (BusinessException exception) {
            binding.reject("categoria", exception.getMessage());
            prepararFormulario(model, categoriaForm);
            return "catalogo/categorias/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Categoria criada com sucesso.");
        return "redirect:/catalogo/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        Categoria categoria = categoriaService.buscarDaInstituicaoAtual(id);
        model.addAttribute("categoriaId", id);
        prepararFormulario(model, categoriaService.paraFormulario(categoria));
        return "catalogo/categorias/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid CategoriaForm categoriaForm,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("categoriaId", id);
            prepararFormulario(model, categoriaForm);
            return "catalogo/categorias/form";
        }
        try {
            categoriaService.atualizar(id, categoriaForm);
        } catch (BusinessException exception) {
            binding.reject("categoria", exception.getMessage());
            model.addAttribute("categoriaId", id);
            prepararFormulario(model, categoriaForm);
            return "catalogo/categorias/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Categoria atualizada com sucesso.");
        return "redirect:/catalogo/categorias";
    }

    private void prepararFormulario(Model model, CategoriaForm form) {
        model.addAttribute("categoriaForm", form);
        model.addAttribute("categoriasPai", categoriaService.listarDaInstituicaoAtual(null));
    }
}
