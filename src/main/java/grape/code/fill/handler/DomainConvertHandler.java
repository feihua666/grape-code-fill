package grape.code.fill.handler;

import com.google.auto.service.AutoService;
import grape.code.fill.DebugTool;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
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
public class DomainConvertHandler extends AbstractProcessor {
    public DomainConvertHandler(){
        super();
        DebugTool.info("DomainConvertHandler ");
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        DebugTool.info("process ");
        for (TypeElement annotation : annotations) {
            DebugTool.info("annotation===" + annotation.getQualifiedName().toString());
        }
        for (Element rootElement : roundEnv.getRootElements()) {
            DebugTool.info("rootElement===" + rootElement.toString());
        }
        return false;
    }
    @Override
    public SourceVersion getSupportedSourceVersion() {
        DebugTool.info("getSupportedSourceVersion =" + SourceVersion.latest());
        return SourceVersion.latest();
    }
}
