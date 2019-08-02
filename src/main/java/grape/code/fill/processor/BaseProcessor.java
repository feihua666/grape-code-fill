package grape.code.fill.processor;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * 基类
 * 相关资料，比较详细：https://blog.csdn.net/a_zhenzhen/article/details/86065063
 * Created by yangwei
 * Created at 2019/8/1 15:53
 */
public abstract class BaseProcessor extends AbstractProcessor {

    protected Messager messager;
    protected JavacTrees trees;
    protected Context context;
    protected JavacElements elementUtils;
    protected TreeMaker treeMaker;
    protected Names names;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        this.elementUtils = (JavacElements) processingEnv.getElementUtils();
        this.treeMaker = TreeMaker.instance(context);
        this.names =Names.instance(context);

    }

    /**
     * 获取被注解的元素
     * @param roundEnv
     * @param a
     * @return
     */
    protected Set<? extends Element> getElementsAnnotatedWith(RoundEnvironment roundEnv,Class<? extends Annotation> a){
        return roundEnv.getElementsAnnotatedWith(a);
    }
    /**
     * 标识符，可以是变量，类型，关键字等等
     * @param name
     * @return
     */
    protected JCTree.JCIdent ident(String name){
        return treeMaker.Ident(nfs(name));
    }
    protected JCTree.JCIdent ident(Name name){
        return treeMaker.Ident(name);
    }
    /**
     * 字面量表达式，如“string”等
     * @param str
     * @return
     */
    protected JCTree.JCLiteral literal(String str){
        return treeMaker.Literal(str);
    }

    /**
     * 字面量表达式，如123等
     * @param i
     * @return
     */
    protected JCTree.JCLiteral literal(int i){
        return treeMaker.Literal(i);
    }
    /**
     * 根据字符串获取Name，（利用Names的fromString静态方法）
     * @param s
     * @return
     */
    protected Name nfs(String s) {
        return names.fromString(s);
    }

    /**
     * 修饰符
     * 如：static public
     * @param flags
     * @return
     */
    protected JCTree.JCModifiers modifiers(int flags){
        return treeMaker.Modifiers(flags);
    }

    /**
     * 创建变量语句
     * 例：JCTree.JCVariableDecl var = makeVarDef(treeMaker.Modifiers(0), "xiao", memberAccess("java.lang.String"), treeMaker.Literal("methodName"));
     * 生成语句为：String xiao = "methodName";
     * 注意编译器优化陷阱，如：User user = new User(); 反编译后可能看到的是new User(); user不见了，实际上是编译器优化掉了没有使用的变量
     * @param modifiers
     * @param varName
     * @param vartype
     * @param init
     * @return
     */
    protected JCTree.JCVariableDecl makeVarDef(JCTree.JCModifiers modifiers, String varName, JCTree.JCExpression vartype, JCTree.JCExpression init) {
        return treeMaker.VarDef(
                modifiers,
                nfs(varName), //名字
                vartype, //类型
                init //初始化语句
        );
    }

    /**
     *
     * @param modifiers
     * @param vartype
     * @param init
     * @return
     */
    protected JCTree.JCVariableDecl makeVarDef(JCTree.JCModifiers modifiers, JCTree.JCExpression vartype, JCTree.JCExpression init) {
        return treeMaker.ReceiverVarDef(modifiers,vartype,init);
    }

    /**
     * 声明整型变量并赋值
     * 例：makeIntegerVarDeclAndAssign("zhen",1)
     * 生成语句为：Integer zhen = 1;
     * @param varName
     * @param value
     * @return
     */
    protected JCTree.JCVariableDecl makeIntegerVarDeclAndAssign(String varName,int value){
        return makeVarDef(treeMaker.Modifiers(Flags.PARAMETER), varName, memberAccess("java.lang.Integer"), literal(value));
    }

    /**
     * 创建 域/方法 的多级访问, 方法的标识只能是最后一个
     * 例如： java.lang.System.out.println
     * @param components
     * @return
     */
    protected JCTree.JCExpression memberAccess(String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = ident(componentArray[0]);
        for (int i = 1; i < componentArray.length; i++) {
            expr = treeMaker.Select(expr, nfs(componentArray[i]));
        }
        return expr;
    }

    /**
     * 给变量赋值
     * 例：makeAssignment(treeMaker.Ident(nfs("xiao")), treeMaker.Literal("assignment test"));
     * 生成的赋值语句为：xiao = "assignment test";
     * @param lhs
     * @param rhs
     * @return
     */
    protected JCTree.JCExpressionStatement makeAssignment(JCTree.JCExpression lhs, JCTree.JCExpression rhs) {
        return exec(assign(lhs, rhs));
    }

    /**
     *
     * @param expression
     * @return
     */
    protected JCTree.JCExpressionStatement exec(JCTree.JCExpression expression){
        return treeMaker.Exec(expression);
    }

    /**
     * = 号赋值
     * @param lhs
     * @param rhs
     * @return
     */
    protected JCTree.JCAssign assign(JCTree.JCExpression lhs, JCTree.JCExpression rhs){
        return treeMaker.Assign(lhs,rhs);
    }

    /**
     *
     * @param typeargs
     * @param fn
     * @param args
     * @return
     */
    protected JCTree.JCMethodInvocation apply(List<JCTree.JCExpression> typeargs, JCTree.JCExpression fn, List<JCTree.JCExpression> args){
        return treeMaker.Apply(typeargs,fn,args);
    }
    /**
     * += 操作
     * @param tag
     * @param lhs 左边
     * @param rhs 右边
     * @return
     */
    protected JCTree.JCAssignOp assignop(JCTree.Tag tag,JCTree.JCExpression lhs, JCTree.JCExpression rhs){
        return treeMaker.Assignop(tag,lhs,rhs);
    }

    /**
     * 一元操作,如：++
     * @param tag
     * @param lhs
     * @return
     */
    protected JCTree.JCUnary unary(JCTree.Tag tag,JCTree.JCExpression lhs){
        return treeMaker.Unary(tag,lhs);
    }
    /**
     * 二元操作表达式
     * @param tag
     * @param lhs
     * @param rhs
     * @return
     */
    protected JCTree.JCBinary binary(JCTree.Tag tag,JCTree.JCExpression lhs, JCTree.JCExpression rhs){
        return treeMaker.Binary(tag,lhs,rhs);
    }
    /**
     * 两个字符串字面量相加并赋值
     * 例：makeAssignmentWithDoubleStr("xiao","-Binary operator one","-Binary operator two");
     * 生成语句为：xiao = "-Binary operator one" + "-Binary operator two";
     * @param var
     * @param plusFirst
     * @param plusSecond
     * @return
     */
    protected JCTree.JCExpressionStatement makeAssignmentPlusStr(String var,String plusFirst,String plusSecond) {

        return makeAssignment(ident(var),binary(JCTree.Tag.PLUS,literal(plusFirst),literal(plusSecond)));

    }

    /**
     * 字符串+=语句
     * 例：makeAssignmentPlusAsgStr("xiao","-Assignop test");
     * 生成语句为：xiao += "-Assignop test";
     * @param varLeft
     * @param rightStr
     * @return
     */
    protected JCTree.JCExpressionStatement makeAssignmentPlusAsgStr(String varLeft,String rightStr) {

        return exec(assignop(JCTree.Tag.PLUS_ASG,ident(varLeft),literal(rightStr)));
    }

    /**
     * 数字 +=
     * @param varLeft
     * @param rightint
     * @return
     */
    protected JCTree.JCExpressionStatement makeAssignmentPlusAsgInt(String varLeft,int rightint) {

        return exec(assignop(JCTree.Tag.PLUS_ASG,ident(varLeft),literal(rightint)));
    }
    /**
     * ++语句
     * 例：makeAssignmentPlusPlus("zhen")
     * 生成语句：zhen++;
     * @param varName
     * @return
     */
    protected JCTree.JCExpressionStatement makeAssignmentPlusPlus(String varName) {

        return exec(unary(JCTree.Tag.PREINC,ident(varName)));
    }

    /**
     * if语句
      * @param condition
     * @param ifBody
     * @param elseBody
     * @return
     */
    protected JCTree.JCIf  makeIf(JCTree.JCExpression condition, JCTree.JCStatement ifBody, JCTree.JCStatement elseBody) {
        return treeMaker.If(condition,ifBody,elseBody);
    }
    /**
     * 加法语句
     * 例：makeAssignmentPlus("zhen","zhen1",10)
     * 生成语句：zhen = zhen1 + 10;
     * @param varLeft
     * @param varRight
     * @param contanst
     * @return
     */
    protected JCTree.JCExpressionStatement makeAssignmentPlus(String varLeft,String varRight,int contanst) {
        return makeAssignment(ident(varLeft),binary(JCTree.Tag.PLUS,ident(varRight),literal(contanst)));
    }
    @Override
    public abstract boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
