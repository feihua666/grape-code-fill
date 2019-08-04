package grape.code.fill.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;
import grape.code.fill.ClassUtils;
import grape.code.fill.annocations.DomainConvert;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @DomainConvert 处理器，暂未实现
 * Created by yangwei
 * Created at 2019/7/28 19:50
 */
@SupportedAnnotationTypes({"grape.code.fill.annocations.DomainConvert"})
@AutoService(Processor.class)
public class DomainConvertProcessor extends BaseProcessor {

    private static String domainConvert = "grape.code.fill.annocations.DomainConvert";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {


        final Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        final JavacElements elementUtils = (JavacElements) processingEnv.getElementUtils();
        final TreeMaker treeMaker = TreeMaker.instance(context);
        final Names names =Names.instance(context);
        Set<? extends Element> elements = roundEnv.getRootElements();

        for (Element element : roundEnv.getElementsAnnotatedWith(DomainConvert.class)) {
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) elementUtils.getTree(element);
            jcMethodDecl.accept(new MethodTreeTranslator());

        }
        return false;
    }

    private class MethodTreeTranslator extends TreeTranslator{
        @Override
        public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
            Symbol.ClassSymbol returnTypeClassSymbol = (Symbol.ClassSymbol) ((JCTree.JCIdent) jcMethodDecl.restype).sym;
            Symbol.ClassSymbol paramTypeClassSymbol = (Symbol.ClassSymbol) ((JCTree.JCIdent) ((JCTree.JCVariableDecl) jcMethodDecl.params.get(0)).vartype).sym;
            String varName = ClassUtils.lowerFirstLetter(returnTypeClassSymbol.name.toString());
            Name paramName = jcMethodDecl.params.get(0).name;

            ListBuffer listBuffer = new ListBuffer<>();
            // 获取注解
            JCTree.JCAnnotation annotation = ClassUtils.getAnnotation(jcMethodDecl,DomainConvert.class);
            // 获取注解定义的默认值
            Boolean checkNullDefault = ClassUtils.getAnnotationValueDefaultBoolean(annotation,"checkNull");
            // 获取注解使用的值
            Boolean checkNull = ClassUtils.getAnnotationValueBoolean(annotation,"checkNull");

            boolean isCheckNull = true;

            if (checkNull == null) {
                isCheckNull = checkNullDefault;
            }else {
                isCheckNull = checkNull;
            }
            // 判断null
            if (isCheckNull) {
                listBuffer.append(makeIf( treeMaker.Binary(
                        JCTree.Tag.EQ,
                        treeMaker.Ident(nfs(paramName.toString())),
                        treeMaker.Literal(TypeTag.BOT, null)),treeMaker.Return(treeMaker.Literal(TypeTag.BOT, null)),null)
                );
            }
            // User user = new User()
            JCTree.JCNewClass newClass = treeMaker.NewClass(null, List.<JCTree.JCExpression>nil(), ident(returnTypeClassSymbol.name), List.<JCTree.JCExpression>nil(), null);

            JCTree.JCVariableDecl newInstance = makeVarDef(treeMaker.Modifiers(Flags.PARAMETER), varName, memberAccess(returnTypeClassSymbol.fullname.toString()), (newClass));
            listBuffer.append(newInstance);

            List<Symbol.MethodSymbol> returnTypeMothedSymbols = ClassUtils.getSetMethodFields(returnTypeClassSymbol,true);
            List<Symbol.MethodSymbol> paramTypeMothedSymbols = ClassUtils.getGetMethodFields(paramTypeClassSymbol,true);

            // set get方法
            for (Symbol.MethodSymbol returnTypeMothedSymbol : returnTypeMothedSymbols) {
                for (Symbol.MethodSymbol paramTypeMothedSymbol : paramTypeMothedSymbols) {
                    if (ClassUtils.isSetGetMethodMacth(returnTypeMothedSymbol.name.toString(),paramTypeMothedSymbol.name.toString())) {
                        listBuffer.append(exec(apply(
                                List.<JCTree.JCExpression>nil(),
                                memberAccess( varName + "." + returnTypeMothedSymbol.name.toString()),
                                List.of(apply(
                                        List.<JCTree.JCExpression>nil(),
                                        memberAccess(paramName.toString() + "." + paramTypeMothedSymbol.name.toString()),
                                        List.<JCTree.JCExpression>nil()
                                ))
                                )));

                    }
                }
            }

            listBuffer.append(treeMaker.Return(ident(varName)));
            // 重写body
            jcMethodDecl.body = treeMaker.Block(0, listBuffer.toList());
            super.visitMethodDef(jcMethodDecl);
        }
    }
}
