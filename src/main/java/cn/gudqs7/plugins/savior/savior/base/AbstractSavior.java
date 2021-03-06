package cn.gudqs7.plugins.savior.savior.base;

import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.pojo.resolver.StructureAndCommentInfo;
import cn.gudqs7.plugins.common.resolver.comment.AnnotationHolder;
import cn.gudqs7.plugins.common.resolver.structure.StructureAndCommentResolver;
import cn.gudqs7.plugins.common.util.structure.PsiAnnotationUtil;
import cn.gudqs7.plugins.common.util.structure.ResolverContextHolder;
import cn.gudqs7.plugins.savior.reader.Java2ApiReader;
import cn.gudqs7.plugins.savior.reader.Java2MapReader;
import cn.gudqs7.plugins.savior.theme.Theme;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wq
 * @date 2021/5/19
 */
public abstract class AbstractSavior<T> extends BaseSavior {

    protected final Java2MapReader java2JsonReader;
    protected final Java2ApiReader java2ApiReader;
    protected final StructureAndCommentResolver structureAndCommentResolver;

    public AbstractSavior(Theme theme) {
        super(theme);
        java2JsonReader = new Java2MapReader(theme);
        java2ApiReader = new Java2ApiReader();
        structureAndCommentResolver = new StructureAndCommentResolver();
    }

    protected int orderByMethod(PsiMethod publicMethod, PsiMethod publicMethod2) {
        PsiAnnotation orderAnnotation = publicMethod.getAnnotation("org.springframework.core.annotation.Order");
        int order = Integer.MAX_VALUE;
        if (orderAnnotation != null) {
            order = PsiAnnotationUtil.getAnnotationValue(orderAnnotation, "value", order);
        }
        PsiAnnotation orderAnnotation2 = publicMethod2.getAnnotation("org.springframework.core.annotation.Order");
        int order2 = Integer.MAX_VALUE;
        if (orderAnnotation2 != null) {
            order2 = PsiAnnotationUtil.getAnnotationValue(orderAnnotation2, "value", order2);
        }
        return order - order2;
    }

    protected boolean filterMethod(PsiMethod method) {
        if (method.isConstructor()) {
            return true;
        }
        PsiModifierList modifierList = method.getModifierList();
        if (modifierList.hasModifierProperty(PsiModifier.STATIC)) {
            return true;
        }
        if (modifierList.hasModifierProperty(PsiModifier.PRIVATE)) {
            return true;
        }
        // ???????????? Lombok ???????????????
        return "LombokLightModifierList".equals(modifierList.toString());
    }

    /**
     * ??????????????????ActionName??????
     *
     * @param method ??????
     * @return ????????????ActionName??????
     */
    protected String getMethodActionName(PsiMethod method) {
        AnnotationHolder psiMethodHolder = AnnotationHolder.getPsiMethodHolder(method);
        CommentInfo propertyForMethod = psiMethodHolder.getCommentInfo();
        if (propertyForMethod != null) {
            return propertyForMethod.getSingleStr(MoreCommentTagEnum.AMP_ACTION_NAME.getTag(), null);
        }
        return null;
    }

    /**
     * ?????? PsiMethod ?????????????????????
     *
     * @param project            ??????
     * @param publicMethod       ??????
     * @param interfaceClassName ????????????
     * @param jumpHidden         ???????????? hidden ??????
     * @return ???????????????
     */
    protected T getDataByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, boolean jumpHidden) {
        return getDataByMethod(project, interfaceClassName, publicMethod, new HashMap<>(2), jumpHidden);
    }


    /**
     * ?????? PsiMethod ?????????????????????
     *
     * @param project            ??????
     * @param publicMethod       ??????
     * @param interfaceClassName ????????????
     * @param param              ????????????
     * @param jumpHidden         ???????????? hidden ??????
     * @return ???????????????
     */
    protected T getDataByMethod(Project project, String interfaceClassName, PsiMethod publicMethod, Map<String, Object> param, boolean jumpHidden) {
        AnnotationHolder annotationHolder = AnnotationHolder.getPsiMethodHolder(publicMethod);
        CommentInfo commentInfo = annotationHolder.getCommentInfo();

        if (!jumpHidden) {
            boolean hidden = commentInfo.isHidden(false);
            if (hidden || theme.handleMethodHidden(annotationHolder)) {
                return null;
            }
        }
        structureAndCommentResolver.setProject(project);

        List<String> hiddenRequest = commentInfo.getHiddenRequest();
        List<String> onlyRequest = commentInfo.getOnlyRequest();
        ResolverContextHolder.addData(ResolverContextHolder.HIDDEN_KEYS, hiddenRequest);
        ResolverContextHolder.addData(ResolverContextHolder.ONLY_KEYS, onlyRequest);

        PsiParameterList parameterTypes = publicMethod.getParameterList();
        StructureAndCommentInfo paramStructureAndCommentInfo = structureAndCommentResolver.resolveFromParameterList(parameterTypes);

        ResolverContextHolder.removeAll();

        List<String> hiddenResponse = commentInfo.getHiddenResponse();
        List<String> onlyResponse = commentInfo.getOnlyResponse();
        ResolverContextHolder.addData(ResolverContextHolder.HIDDEN_KEYS, hiddenResponse);
        ResolverContextHolder.addData(ResolverContextHolder.ONLY_KEYS, onlyResponse);

        PsiTypeElement returnTypeElement = publicMethod.getReturnTypeElement();
        StructureAndCommentInfo returnStructureAndCommentInfo = structureAndCommentResolver.resolveFromReturnVal(returnTypeElement);

        ResolverContextHolder.removeAll();

        return getDataByStructureAndCommentInfo(
                project, publicMethod, commentInfo, interfaceClassName,
                paramStructureAndCommentInfo, returnStructureAndCommentInfo, param
        );
    }

    /**
     * ????????????/????????????????????????????????????
     *
     * @param project                       ??????
     * @param publicMethod                  ??????
     * @param commentInfo                   ????????????/????????????
     * @param interfaceClassName            ????????????
     * @param paramStructureAndCommentInfo  ????????????+????????????
     * @param returnStructureAndCommentInfo ???????????????+????????????
     * @param param                         ????????????
     * @return ???????????????
     */
    protected abstract T getDataByStructureAndCommentInfo(
            Project project, PsiMethod publicMethod, CommentInfo commentInfo, String interfaceClassName,
            StructureAndCommentInfo paramStructureAndCommentInfo,
            StructureAndCommentInfo returnStructureAndCommentInfo,
            Map<String, Object> param);

}
