package br.dev.lourenco.scriba.core.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    Object handleResourceNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return adapt(request, HttpStatus.NOT_FOUND, "Recurso não encontrado", exception.getMessage(),
            "https://scriba.dev/errors/not-found");
    }

    @ExceptionHandler(BusinessException.class)
    Object handleBusiness(BusinessException exception, HttpServletRequest request) {
        return adapt(request, HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio violada", exception.getMessage(),
            "https://scriba.dev/errors/business");
    }

    @ExceptionHandler(TenantIsolationViolationException.class)
    Object handleTenantViolation(TenantIsolationViolationException exception, HttpServletRequest request) {
        return adapt(request, HttpStatus.FORBIDDEN, "Violação de isolamento de tenant", exception.getMessage(),
            "https://scriba.dev/errors/tenant-isolation");
    }

    @ExceptionHandler(Exception.class)
    Object handleUnexpected(Exception exception, HttpServletRequest request) {
        return adapt(request, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", exception.getMessage(),
            "https://scriba.dev/errors/internal");
    }

    private Object adapt(HttpServletRequest request, HttpStatus status, String title, String detail, String type) {
        if (isHtmx(request)) {
            ModelAndView modelAndView = new ModelAndView("error/htmx-error");
            modelAndView.setStatus(status);
            modelAndView.addObject("status", status.value());
            modelAndView.addObject("message", detail);
            return modelAndView;
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(java.net.URI.create(type));
        problemDetail.setInstance(java.net.URI.create(request.getRequestURI()));
        return org.springframework.http.ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(problemDetail);
    }

    private boolean isHtmx(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getHeader("HX-Request"));
    }
}
