package run.halo.starter;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(kind = "FbInfo", group = "wxgzh-helper.mk95.cn",
    version = "v1", singular = "fbInfo", plural = "fbInfos")
public class WxgzhFbInfo extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private FbInfoSepc spec;

    @Data
    public static class FbInfoSepc {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String postName;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String fbzt;

        private String fbcz;

        private String wxtId; // 外系统id
    }
}
