package cn.gudqs7.plugins.savior.action.restful;

import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.savior.action.base.AbstractProjectDocerSavior;
import cn.gudqs7.plugins.savior.savior.more.JavaToDocSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

/**
 * @author wq
 */
public class RestfulProjectDocSaviorAction extends AbstractProjectDocerSavior {

    public RestfulProjectDocSaviorAction() {
        super(new JavaToDocSavior(ThemeHelper.getRestfulTheme()));
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        return PsiClassUtil.isControllerOrFeign(psiClass);
    }

    @Override
    protected String getDirPrefix() {
        return "restful";
    }
}
