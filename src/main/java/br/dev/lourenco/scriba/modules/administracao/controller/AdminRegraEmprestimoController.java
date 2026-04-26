package br.dev.lourenco.scriba.modules.administracao.controller;

import br.dev.lourenco.scriba.modules.administracao.dto.RegraEmprestimoForm;
import br.dev.lourenco.scriba.modules.administracao.service.InstituicaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/regras-emprestimo")
public class AdminRegraEmprestimoController {

    private final InstituicaoService instituicaoService;

    public AdminRegraEmprestimoController(InstituicaoService instituicaoService) {
        this.instituicaoService = instituicaoService;
    }

    @GetMapping
    public String editar(Model model) {
        model.addAttribute("regraEmprestimoForm", instituicaoService.regrasAtuaisComoFormulario());
        return "admin/regras-emprestimo/form";
    }

    @PostMapping
    public String atualizar(
        @Valid @ModelAttribute("regraEmprestimoForm") RegraEmprestimoForm form,
        BindingResult binding,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            return "admin/regras-emprestimo/form";
        }

        instituicaoService.atualizarRegras(form);
        redirectAttributes.addFlashAttribute("sucesso", "Regras de empréstimo atualizadas.");
        return "redirect:/admin/regras-emprestimo";
    }
}
