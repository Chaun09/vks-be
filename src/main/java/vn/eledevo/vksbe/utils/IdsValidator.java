package vn.eledevo.vksbe.utils;

import java.lang.reflect.Field;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import vn.eledevo.vksbe.constant.ResponseMessage;

public class IdsValidator implements ConstraintValidator<ValidIds, Object> {
    private String[] fields;

    @Override
    public void initialize(ValidIds constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        try {
            for (String field : fields) {
                Field declaredField = value.getClass().getDeclaredField(field);
                declaredField.setAccessible(true);
                Object fieldValue = declaredField.get(value);

                if (fieldValue == null || (fieldValue instanceof Long && (Long) fieldValue == 0)) {
                    String message = "";
                    switch (field) {
                        case "roleId":
                            message = ResponseMessage.ROLE_ID_NOT_NULL;
                            break;
                        case "departmentId":
                            message = ResponseMessage.DEPARTMENT_ID_NOT_NULL;
                            break;
                        case "organizationId":
                            message = ResponseMessage.ORGANIZATION_ID_NOT_NULL;
                            break;
                        default:
                            message = "Không được để trống.";
                    }

                    context.buildConstraintViolationWithTemplate(message)
                            .addPropertyNode(field)
                            .addConstraintViolation();

                    isValid = false;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return isValid;
    }
}
