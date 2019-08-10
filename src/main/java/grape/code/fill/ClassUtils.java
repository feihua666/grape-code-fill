package grape.code.fill;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import java.util.Iterator;

/**
 * Created by yangwei
 * Created at 2019/7/31 8:52
 */
public class ClassUtils {

    static String set = "set";
    static String get = "get";

    public static List<Symbol.MethodSymbol> getSetMethodFields(Symbol.ClassSymbol c, boolean includeSuper){
        return getMethodFields(c, set,includeSuper);
    }
    public static List<Symbol.MethodSymbol> getGetMethodFields(Symbol.ClassSymbol c, boolean includeSuper){
        return getMethodFields(c, get,includeSuper);
    }
    public static List<Symbol.MethodSymbol> getMethodFields(Symbol.ClassSymbol c,String methodPrefix, boolean includeSuper){
        if(c.members_field != null && c.members_field.getElements() !=null){

        }else {
            return List.nil();
        }
        Iterator<Symbol> iterator =  c.members_field.getElements().iterator();
        Symbol symbol = null;
        ListBuffer list = new ListBuffer();
        while ((symbol = iterator.next()) != null){
            if (symbol instanceof Symbol.MethodSymbol) {
                String name = symbol.name.toString();
                if (methodPrefix != null) {
                    if (name.startsWith(methodPrefix)) {
                        list.append((Symbol.MethodSymbol) symbol);
                    }
                }else{
                    list.append((Symbol.MethodSymbol) symbol);
                }

            }

        }
        if (includeSuper) {
            Symbol.ClassSymbol superClass = (Symbol.ClassSymbol) c.getSuperclass().asElement();
            if (!superClass.fullname.toString().equals(Object.class.getCanonicalName())) {
                c = superClass;
                list.addAll(getMethodFields(c,methodPrefix,includeSuper));
            }
        }


        return list.toList();
    }

    /**
     * set get方法是否匹配
     * @param setMethodName
     * @param getMethodName
     * @return
     */
    public static boolean isSetGetMethodMacth(String setMethodName,String getMethodName){

        return setMethodName.startsWith(set) && getMethodName.startsWith(get)
                && setMethodName.substring(1).equals(getMethodName.substring(1));
    }


    /**
     * 获取注解
     * @param jcMethodDecl
     * @param anno 注解的class
     * @return
     */
    public static JCTree.JCAnnotation getAnnotation(JCTree.JCMethodDecl jcMethodDecl,Class anno){
        for (JCTree.JCAnnotation annotation : jcMethodDecl.mods.getAnnotations()) {
            if (((JCTree.JCIdent) annotation.annotationType).sym.getQualifiedName().toString().equals(anno.getCanonicalName())) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * 获取注解声明的布尔类型默认值
     * @param annotation
     * @param fieldName
     * @return
     */
    public static Boolean getAnnotationValueDefaultBoolean(JCTree.JCAnnotation annotation, String fieldName){
        Iterator<Symbol> iterator = ((JCTree.JCIdent) annotation.annotationType).sym.members().getElements().iterator();
        Symbol symbol = null;
        while ((symbol = iterator.next()) != null){
            if (symbol.getSimpleName().toString().equals(fieldName)) {
                return (Boolean) ((Symbol.MethodSymbol) symbol).defaultValue.getValue();
            }
        }
        return null;
    }

    /**
     * 获取注解属性的布尔值
     * @param annotation
     * @param fieldName
     * @return null=未定义
     */
    public static Boolean getAnnotationValueBoolean(JCTree.JCAnnotation annotation, String fieldName){
        for (JCTree.JCExpression arg : annotation.args) {
            if (arg instanceof JCTree.JCAssign) {
                if (((JCTree.JCIdent) ((JCTree.JCAssign) arg).lhs).name.toString().equals(fieldName)) {
                    return (Boolean) ((JCTree.JCLiteral) ((JCTree.JCAssign) arg).rhs).getValue();
                }
            }
        }
        return null;
    }


    /**
     * 首字母转小写
     * @param str
     * @return
     */
    public static String  lowerFirstLetter(String str){
        return str.substring(0,1).toLowerCase() + str.substring(1);
    }
    public static String dotCat(String ...strings){
        StringBuffer sb = new StringBuffer();
        for (String string : strings) {
            sb.append(string).append(".");
        }
        return sb.substring(0,sb.length()-1);
    }

}
