package br.dev.lourenco.scriba.modules.notificacao.service;

import br.dev.lourenco.scriba.modules.circulacao.domain.Emprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.Reserva;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);

    private final JavaMailSender mailSender;
    private final String remetente;

    public NotificacaoService(JavaMailSender mailSender, @Value("${scriba.mail.from:no-reply@scriba.dev}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public boolean notificarReservaConfirmada(Reserva reserva) {
        String assunto = "Reserva confirmada - Scriba";
        String corpo = "Sua reserva para o item \"" + reserva.getAcervoItem().getTitulo()
            + "\" foi registrada. Posicao na fila: " + reserva.getPosicaoFila() + ".";
        return enviar(reserva.getLeitor().getEmail(), assunto, corpo, "Reserva confirmada #" + reserva.getId());
    }

    public boolean notificarAtraso(Emprestimo emprestimo) {
        String assunto = "Emprestimo em atraso - Scriba";
        String corpo = "O item \"" + emprestimo.getAcervoItem().getTitulo()
            + "\" venceu em " + emprestimo.getDataPrevistaDevolucao() + ".";
        return enviar(emprestimo.getLeitor().getEmail(), assunto, corpo, "Atraso notificado #" + emprestimo.getId());
    }

    private boolean enviar(String destinatario, String assunto, String corpo, String logSucesso) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpo, false);
            mailSender.send(message);
            log.info("E-mail enviado para {} - {}", destinatario, logSucesso);
            return true;
        } catch (MailException | MessagingException ex) {
            log.warn("Falha ao enviar e-mail para {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }
}
