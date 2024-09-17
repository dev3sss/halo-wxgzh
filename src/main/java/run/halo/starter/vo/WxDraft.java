package run.halo.starter.vo;

import lombok.Data;

@Data
public class WxDraft {
    String title;
    String author;
    String wxContent;
    String ywUrl; // 原文url
    String mediaId; // 封面图片id
}
