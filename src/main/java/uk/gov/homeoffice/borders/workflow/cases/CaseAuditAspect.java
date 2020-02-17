package uk.gov.homeoffice.borders.workflow.cases;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import uk.gov.homeoffice.borders.workflow.exception.ForbiddenException;
import uk.gov.homeoffice.borders.workflow.identity.PlatformUser;

import java.util.Arrays;

@Component
@Aspect
@Slf4j
public class CaseAuditAspect {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Around("@annotation(AuditableCaseEvent)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = Arrays.stream(joinPoint.getArgs()).filter( arg -> !(arg instanceof PlatformUser)).toArray();

        PlatformUser platformUser= (PlatformUser) Arrays
                .stream(joinPoint.getArgs()).filter( arg -> arg instanceof PlatformUser)
                .findAny().orElseThrow(() -> new ForbiddenException("User not found"));

        CaseAudit caseAudit = new CaseAudit(
                joinPoint.getTarget(),
                args,
                platformUser,
                joinPoint.getSignature().toShortString());
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            log.info("Total time taken for '{}' was '{}' seconds",
                    joinPoint.getSignature().toShortString(),
                    stopWatch.getTotalTimeSeconds());

            applicationEventPublisher.publishEvent(caseAudit);
        }
    }
}
