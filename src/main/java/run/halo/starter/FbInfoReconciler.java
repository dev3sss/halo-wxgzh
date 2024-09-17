package run.halo.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.plugin.ReactiveSettingFetcher;

@Slf4j
@Component
@RequiredArgsConstructor
public class FbInfoReconciler implements Reconciler<Reconciler.Request> {

    private final ReactiveExtensionClient client;

    private final ReactiveSettingFetcher settingFetcher;

    @Override
    public Result reconcile(Request request) {
        settingFetcher.fetch(MateSetting.GROUP, MateSetting.class)
            .flatMap(settingFetcher -> {
                // 获取配置信息
                log.debug("wxgzh-helper setting: {}", settingFetcher);
                return client.fetch(WxgzhFbInfo.class, request.name()).map(fbInfo -> {
                    log.info("请求时: {}", fbInfo);
                    // fbInfo.getSpec().setFbzt("3");
                    // fbInfo.getSpec().setFbcz(settingFetcher.fbcz());
                    // return client.update(fbInfo);
                    return fbInfo;
                });
            }).subscribe();

        return null;
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder.extension(new WxgzhFbInfo())
            .build();
    }
}
