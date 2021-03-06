package cn.gudqs7.plugins.savior.action.batch;

import cn.gudqs7.plugins.common.base.action.AbstractBatchDocerSavior;
import cn.gudqs7.plugins.common.enums.PluginSettingEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.util.PluginSettingHelper;
import cn.gudqs7.plugins.common.util.jetbrain.IdeaApplicationUtil;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.savior.savior.more.JavaToOneApiSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 导出相应信息成 OneApi
 *
 * @author wenquan
 * @date 2022/3/30
 */
public class OneApiDocerSaviorAction extends AbstractBatchDocerSavior {

    protected JavaToOneApiSavior javaToOneApiSavior;

    public OneApiDocerSaviorAction() {
        this.javaToOneApiSavior = new JavaToOneApiSavior(ThemeHelper.getRestfulTheme());
    }

    @Override
    protected boolean isNotShow(@NotNull AnActionEvent e, Project project, PsiElement psiElement, PsiClass psiClass, PsiDirectory psiDirectory) {
        return !PluginSettingHelper.getConfigItem(PluginSettingEnum.ONE_API_ENABLE, false);
    }

    @Override
    protected void runLoop(Project project, PsiClass psiClass0, AtomicBoolean hasCancelAtomic, CommentInfo commentInfo, String moduleName, String fileName, File parent, String fileParentDir, String fullFileName, Map<String, Object> otherMap, ProgressIndicator indicator, float fraction) {
        indicator.setText2("处理中：" + moduleName + " - " + commentInfo.getItemName(psiClass0.getName()));
        indicator.setFraction(fraction);

        IdeaApplicationUtil.runReadAction(() -> {
            javaToOneApiSavior.generateOneApi(psiClass0, project);
        });
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        return PsiClassUtil.isControllerOrFeign(psiClass);
    }

    @Override
    protected String getDirPrefix() {
        return "oneApi";
    }

    @Override
    protected @NotNull String getFileExtension() {
        return "yaml";
    }
}
