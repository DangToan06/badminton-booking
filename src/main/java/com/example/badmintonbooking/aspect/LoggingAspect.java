package com.example.badmintonbooking.aspect;

import com.example.badmintonbooking.dto.response.BookingDTO;
import com.example.badmintonbooking.security.principal.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.example.badmintonbooking.service..*(..))")
    public void allServiceMethods() {}

    @Pointcut("execution(* com.example.badmintonbooking.service.IBookingService.createBooking(..))")
    public void createBookingPointcut() {}

    @Around("allServiceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getTarget().getClass().getSimpleName();
        String className = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();

        try{

            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;

            if(duration > 2000){
                log.warn("[PERF - SLOW] {}.{}() completed in {} ms. Exceeds 2s threshold", className, methodName, duration);
            }else {
                log.info("[PERF] {}.{}() completed in {} ms.", className, methodName, duration);
            }

            return result;
        }catch(Throwable ex){
            long duration = System.currentTimeMillis() - startTime;
            log.error("[PERP - ERROR] {}.{}() failed after {} ms | {}:{}]",  className, methodName, duration, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }

    }

    @AfterReturning(
            pointcut = "createBookingPointcut()",
            returning = "result"
    )
    public void logBookingSuccess(JoinPoint joinPoint, Object result) {
        try {
            UserPrincipal principal = extractPrincipal(joinPoint.getArgs());
            BookingDTO booking = (BookingDTO) result;

            log.info(
                    "[AUDIT - SUCCESS] Khách hàng '{}' đặt thành công '{}' vào ngày {}, Khung giờ {}. Booking ID: {}",
                    principal != null ? principal.getUsername() : "unknown",
                    booking.getCourtName(),
                    booking.getBookingDate(),
                    booking.getTimeSlot(),
                    booking.getId()
            );

        }catch (Exception e){
            log.warn("[AUDIT] Could not log booking success: {}", e.getMessage());
        }
    }

    @AfterThrowing(
            pointcut = "createBookingPointcut()",
            throwing  = "ex"
    )
    public void logBookingFailed(JoinPoint joinPoint, Exception ex) {
        try {
            UserPrincipal principal = extractPrincipal(joinPoint.getArgs());

            log.warn(
                    "[AUDIT - FAILED] Khách hàng '{}' cố gắng đặt sân nhưng thất bại. Lý do: {}",
                    principal != null ? principal.getUsername() : "unknown",
                    ex.getMessage()
            );
        } catch (Exception e) {
            log.warn("[AUDIT] Could not log booking failure: {}", e.getMessage());

        }
    }

    private UserPrincipal extractPrincipal(Object[] args) {
        return Arrays.stream(args)
                .filter(arg -> arg instanceof UserPrincipal)
                .map(arg -> (UserPrincipal) arg)
                .findFirst()
                .orElse(null);
    }

}
