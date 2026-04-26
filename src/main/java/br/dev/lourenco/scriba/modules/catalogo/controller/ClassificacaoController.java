package br.dev.lourenco.scriba.modules.catalogo.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.catalogo.domain.Classificacao;
import br.dev.lourenco.scriba.modules.catalogo.domain.PadraoClassificacao;
import br.dev.lourenco.scriba.modules.catalogo.dto.ClassificacaoForm;
import br.dev.lourenco.scriba.modules.catalogo.service.ClassificacaoService;
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
@RequestMapping("/catalogo/classificacoes")
public class ClassificacaoController {

    private final ClassificacaoService classificacaoService;

    public ClassificacaoController(ClassificacaoService classificacaoService) {
        this.classificacaoService = classificacaoService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "busca", required = false) String busca, Model model) {
        model.addAttribute("classificacoes", classificacaoService.listarDaInstituicaoAtual(busca));
        model.addAttribute("busca", busca);
        return "catalogo/classificacoes/list";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        prepararFormulario(model, new ClassificacaoForm());
        return "catalogo/classificacoes/form";
    }

    @PostMapping
    public String criar(
        @Valid ClassificacaoForm classificacaoForm,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            prepararFormulario(model, classificacaoForm);
            return "catalogo/classificacoes/form";
        }
        try {
            classificacaoService.criar(classificacaoForm);
        } catch (BusinessException exception) {
            binding.reject("classificacao", exception.getMessage());
            prepararFormulario(model, classificacaoForm);
            return "catalogo/classificacoes/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Classificação criada com sucesso.");
        return "redirect:/catalogo/classificacoes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        Classificacao classificacao = classificacaoService.buscarDaInstituicaoAtual(id);
        model.addAttribute("classificacaoId", id);
        prepararFormulario(model, classificacaoService.paraFormulario(classificacao));
        return "catalogo/classificacoes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid ClassificacaoForm classificacaoForm,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("classificacaoId", id);
            prepararFormulario(model, classificacaoForm);
            return "catalogo/classificacoes/form";
        }
        try {
            classificacaoService.atualizar(id, classificacaoForm);
        } catch (BusinessException exception) {
            binding.reject("classificacao", exception.getMessage());
            model.addAttribute("classificacaoId", id);
            prepararFormulario(model, classificacaoForm);
            return "catalogo/classificacoes/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Classificação atualizada com sucesso.");
        return "redirect:/catalogo/classificacoes";
    }

    private void prepararFormulario(Model model, ClassificacaoForm form) {
        model.addAttribute("classificacaoForm", form);
        model.addAttribute("padroes", PadraoClassificacao.values());
    }
}
