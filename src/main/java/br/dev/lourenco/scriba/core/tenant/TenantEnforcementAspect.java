package br.dev.lourenco.scriba.core.tenant;

import java.util.Arrays;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.TenantIsolationViolationException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Aspect
@Component
public class TenantEnforcementAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantEnforcementAspect.class);

    private final TenantContext tenantContext;
    private final TenantProperties tenantProperties;

    public TenantEnforcementAspect(TenantContext tenantContext, TenantProperties tenantProperties) {
        this.tenantContext = tenantContext;
        this.tenantProperties = tenantProperties;
    }

    @PostConstruct
    void logConfiguration() {
        log.info("TenantEnforcementAspect: Pointcut ativo em br.dev.lourenco.scriba.modules.*.repository.*");
        log.info("TenantEnforcementAspect: scriba.tenant.strict-mode={}", tenantProperties.isStrictMode());
    }

    @Around("execution(* br.dev.lourenco.scriba.modules..repository..*(..))")
    public Object enforce(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!isTenantScoped(joinPoint)) {
            return joinPoint.proceed();
        }

        if (!tenantProperties.isStrictMode()) {
            return joinPoint.proceed();
        }

        return tenantContext.instituicaoId()
            .map(expectedTenant -> proceedWithValidation(joinPoint, expectedTenant))
            .orElseGet(() -> proceedUnchecked(joinPoint));
    }

    private Object proceedWithValidation(ProceedingJoinPoint joinPoint, UUID expectedTenant) {
        try {
            if (requiresTenantArgument(joinPoint) && !hasExpectedTenant(joinPoint.getArgs(), expectedTenant)) {
                throw new TenantIsolationViolationException(
                    "A chamada ao repositório deve incluir o instituicaoId do tenant autenticado.");
            }
            return joinPoint.proceed();
        } catch (TenantIsolationViolationException exception) {
            throw exception;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private Object proceedUnchecked(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private boolean requiresTenantArgument(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        return methodName.startsWith("find") || methodName.startsWith("exists") || methodName.startsWith("count");
    }

    private boolean isTenantScoped(ProceedingJoinPoint joinPoint) {
        Class<?> targetClass = joinPoint.getTarget().getClass();
        if (targetClass.isAnnotationPresent(TenantScopedRepository.class)) {
            return true;
        }

        for (Class<?> type : targetClass.getInterfaces()) {
            if (type.isAnnotationPresent(TenantScopedRepository.class)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasExpectedTenant(Object[] args, UUID expectedTenant) {
        return Arrays.stream(args)
            .filter(UUID.class::isInstance)
            .map(UUID.class::cast)
            .anyMatch(expectedTenant::equals);
    }
}
