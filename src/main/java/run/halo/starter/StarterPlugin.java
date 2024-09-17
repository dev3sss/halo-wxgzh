package run.halo.starter;

import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author guqing
 * @since 1.0.0
 */
@Component
public class StarterPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public StarterPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(WxgzhFbInfo.class);
        System.out.println("插件启动成功！");
    }

    @Override
    public void stop() {
        Scheme fbInfoScheme = schemeManager.get(WxgzhFbInfo.class);
        schemeManager.unregister(fbInfoScheme);
        System.out.println("插件停止！");
    }
}
