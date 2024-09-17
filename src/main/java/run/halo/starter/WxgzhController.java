package run.halo.starter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ApiVersion;
import run.halo.starter.vo.WxDraft;
import run.halo.starter.wxgzh.WxgzhApi;

@ApiVersion("console.api.wxgzh-helper.mk95.cn/v1")
@RequestMapping("/wx")
@RestController
@AllArgsConstructor
@Slf4j
public class WxgzhController {

    private final ReactiveExtensionClient client;

    private final WxgzhApi wxgzhApi;

    @PostMapping("/fb")
    public Mono<WxgzhFbInfo> fb(@RequestBody WxgzhFbInfo fbInfo) {
        log.debug("发布信息 fbInfo:{}", fbInfo);
        return client.create(fbInfo);
    }

    @PostMapping("/content2WxDraft")
    public Mono<String> content2WxDraft(@RequestBody String htmlContent) throws Exception {
        return Mono.just(wxgzhApi.html2WxDraft(htmlContent));
    }

    @PostMapping("/addDraft")
    public Mono<String> addDraft(@RequestBody WxDraft draft) throws Exception {
        String draftContent = wxgzhApi.addDraft(draft.getTitle(), draft.getAuthor(), draft.getWxContent(),
            draft.getYwUrl(), draft.getMediaId());
        return Mono.just(draftContent);
    }

}
