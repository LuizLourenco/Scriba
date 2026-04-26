package br.dev.lourenco.scriba.modules.administracao.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.administracao.domain.Role;
import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import br.dev.lourenco.scriba.modules.administracao.dto.UsuarioForm;
import br.dev.lourenco.scriba.modules.administracao.service.BibliotecaService;
import br.dev.lourenco.scriba.modules.administracao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;
    private final BibliotecaService bibliotecaService;

    public AdminUsuarioController(UsuarioService usuarioService, BibliotecaService bibliotecaService) {
        this.usuarioService = usuarioService;
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuariosDaInstituicaoAtual());
        return "admin/usuarios/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormulario(model, new UsuarioForm());
        return "admin/usuarios/form";
    }

    @PostMapping
    public String criar(
        @Valid @ModelAttribute("usuarioForm") UsuarioForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (!org.springframework.util.StringUtils.hasText(form.getSenha())) {
            binding.rejectValue("senha", "NotBlank", "Senha é obrigatória.");
        }
        if (binding.hasErrors()) {
            prepararFormulario(model, form);
            return "admin/usuarios/form";
        }

        try {
            usuarioService.criar(form);
        } catch (BusinessException exception) {
            binding.reject("usuario", exception.getMessage());
            prepararFormulario(model, form);
            return "admin/usuarios/form";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Usuário criado com sucesso.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        Usuario usuario = usuarioService.buscarDaInstituicaoAtual(id);
        model.addAttribute("usuarioId", id);
        prepararFormulario(model, usuarioService.paraFormulario(usuario));
        return "admin/usuarios/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid @ModelAttribute("usuarioForm") UsuarioForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("usuarioId", id);
            prepararFormulario(model, form);
            return "admin/usuarios/form";
        }

        try {
            usuarioService.atualizar(id, form);
        } catch (BusinessException exception) {
            binding.reject("usuario", exception.getMessage());
            model.addAttribute("usuarioId", id);
            prepararFormulario(model, form);
            return "admin/usuarios/form";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso.");
        return "redirect:/admin/usuarios";
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
        usuarioService.alterarAtivo(id, ativo);
        model.addAttribute("usuarios", usuarioService.listarUsuariosDaInstituicaoAtual());
        return "admin/usuarios/list :: tabela";
    }

    private void prepararFormulario(Model model, UsuarioForm form) {
        model.addAttribute("usuarioForm", form);
        model.addAttribute("roles", Role.values());
        model.addAttribute("bibliotecas", bibliotecaService.listarDaInstituicaoAtual());
    }
}
