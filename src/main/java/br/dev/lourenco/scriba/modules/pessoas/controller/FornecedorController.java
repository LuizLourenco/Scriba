package br.dev.lourenco.scriba.modules.pessoas.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.pessoas.domain.Fornecedor;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoFornecedor;
import br.dev.lourenco.scriba.modules.pessoas.dto.FornecedorForm;
import br.dev.lourenco.scriba.modules.pessoas.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pessoas/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", fornecedorService.listarDaInstituicaoAtual());
        return "pessoas/fornecedores/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormulario(model, new FornecedorForm());
        return "pessoas/fornecedores/form";
    }

    @PostMapping
    public String criar(
        @Valid @ModelAttribute("fornecedorForm") FornecedorForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            prepararFormulario(model, form);
            return "pessoas/fornecedores/form";
        }

        try {
            fornecedorService.criar(form);
        } catch (BusinessException exception) {
            binding.reject("fornecedor", exception.getMessage());
            prepararFormulario(model, form);
            return "pessoas/fornecedores/form";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Fornecedor criado com sucesso.");
        return "redirect:/pessoas/fornecedores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        Fornecedor fornecedor = fornecedorService.buscarDaInstituicaoAtual(id);
        model.addAttribute("fornecedorId", id);
        prepararFormulario(model, fornecedorService.paraFormulario(fornecedor));
        return "pessoas/fornecedores/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid @ModelAttribute("fornecedorForm") FornecedorForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("fornecedorId", id);
            prepararFormulario(model, form);
            return "pessoas/fornecedores/form";
        }

        try {
            fornecedorService.atualizar(id, form);
        } catch (BusinessException exception) {
            binding.reject("fornecedor", exception.getMessage());
            model.addAttribute("fornecedorId", id);
            prepararFormulario(model, form);
            return "pessoas/fornecedores/form";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Fornecedor atualizado com sucesso.");
        return "redirect:/pessoas/fornecedores";
    }

    private void prepararFormulario(Model model, FornecedorForm form) {
        model.addAttribute("fornecedorForm", form);
        model.addAttribute("tiposFornecedor", TipoFornecedor.values());
    }
}
