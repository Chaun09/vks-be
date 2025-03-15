package vn.eledevo.vksbe.utils.TrimData;

import java.lang.reflect.Field;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TrimProcessor {

    @Before("execution(* vn.eledevo.vksbe.controller..*.*(..))")
    public void trimStringsInController(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg != null) {
                // Nếu class có annotation @Trimmed, trim tất cả các field kiểu String
                if (arg.getClass().isAnnotationPresent(Trimmed.class)) {
                    trimFields(arg);
                }
            }
        }
    }

    private void trimFields(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) { // Chỉ trim các field kiểu String
                field.setAccessible(true);
                try {
                    String value = (String) field.get(obj);
                    if (value != null) {
                        field.set(obj, value.trim()); // Thực hiện trim
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
