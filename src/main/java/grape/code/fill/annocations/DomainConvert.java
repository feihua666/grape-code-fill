package grape.code.fill.annocations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将领域对象转成另一个对象
 * 如：po转vo，无非是new一个对象然后一顿get和set，加个注解自动转换
 * Created by yangwei
 * Created at 2019/7/28 19:25
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface DomainConvert {
    // 判断是否检查是否为空
    boolean checkNull() default true;
}
