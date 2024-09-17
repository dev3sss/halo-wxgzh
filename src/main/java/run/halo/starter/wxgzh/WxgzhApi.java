package run.halo.starter.wxgzh;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.starter.MateSetting;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WxgzhApi {

    private final ReactiveSettingFetcher settingFetcher;
    String access_token_url;

    String access_token;
    OkHttpClient httpClient = new OkHttpClient();

    //    五分钟内新旧token可同时使用，故过期时间提前五分钟
    long expires_time = 0;

    public WxgzhApi(ReactiveSettingFetcher settingFetcher) {
        this.settingFetcher = settingFetcher;
        settingFetcher.fetch(MateSetting.GROUP, MateSetting.class)
            .map(gzhConfig -> {
                // 获取配置信息
                this.access_token_url =
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                        + gzhConfig.appid()
                        + "&secret=" + gzhConfig.secret();
                return gzhConfig;
            }).subscribe();
    }

    /**
     * 获取token
     *
     * @return null
     */
    public String getToken() {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        if (this.expires_time < timeInMillis) {
            this.refreshToken();
        }
        return this.access_token;
    }

    /**
     * 刷新token
     */
    private void refreshToken() {
        settingFetcher.fetch(MateSetting.GROUP, MateSetting.class)
            .map(gzhConfig -> {
                // 获取配置信息
                this.access_token_url =
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                        + gzhConfig.appid()
                        + "&secret=" + gzhConfig.secret();
                return gzhConfig;
            }).subscribe();
        Request request = new Request.Builder().url(this.access_token_url).build();
        Call call = httpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject jsonObject = getResponseJson(response, "更新token失败");
            this.access_token = jsonObject.getString("access_token");
            int expires_in = jsonObject.getInt("expires_in");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, expires_in - 300);
            this.expires_time = calendar.getTimeInMillis();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取body的json内容，含响应检查
     *
     * @param response 响应
     * @param msg 错误提示
     * @return json
     * @throws Exception 异常
     */
    private JSONObject getResponseJson(Response response, String msg) throws Exception {
        if (response.isSuccessful()) {
            assert response.body() != null;
            String body = response.body().string();

            JSONObject jsonObject = new JSONObject(body);
            if (jsonObject.has("errcode") && !"0".equals(jsonObject.get("errcode").toString())) {
                String errmsg = jsonObject.getString("errmsg");
                String errcode = jsonObject.get("errcode").toString();
                throw new Exception(msg + ",errcode:" + errcode + ",errmsg:" + errmsg);
            }
            return jsonObject;
        } else {
            throw new Exception(msg);
        }
    }

    /**
     * 上传图文消息内的图片获取URL
     *
     * @param filename 文件路径
     * @return url
     */
    public String uploadContentImg(String filename) {
        // POST https://api.weixin.qq.com/cgi-bin/media/uploadimg
        String url = "https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=" + getToken();
        File file = new File(filename);
        MediaType type = MediaType.parse("application/octet-stream");//"text/xml;charset=utf-8"
        RequestBody fileBody = RequestBody.create(type, file);
        RequestBody multipartBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("media", file.getName(), fileBody)
            .addFormDataPart("access_token", getToken())
            .build();
        Request request = new Request.Builder()
            .url(url)
            .post(multipartBody)
            .build();
        try {
            Response response = httpClient.newCall(request).execute();
            JSONObject jsonObject =
                getResponseJson(response, "上传图文消息内的图片到微信获取URL失败");
            return jsonObject.getString("url");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将html内容转换为微信公众号草稿内容
     *
     * @param htmlContent html标准内容，将过滤js等内容
     * @return 微信公众号草稿内容
     * @throws Exception 异常
     */
    public String html2WxDraft(String htmlContent) throws Exception {
        if ("".equals(htmlContent) || htmlContent == null) {
            return "";
        }
        final String[] wxDraft = {htmlContent};
        List<String> imgUrls = HtmlUtils.getImgUrls(htmlContent);
        Map<String, String> replaceUrlMap = new HashMap<>();
        for (String imgUrl : imgUrls) {
            String[] split = imgUrl.split("/");
            String fileName = split[split.length - 1];
            String url = imgUrl;
            // 下载文件到本地
            String downFilePath = HtmlUtils.downFile(url, "/temp", fileName);
            // 上传文件到微信，获取微信草稿要使用的url
            String wxUrl = uploadContentImg(downFilePath);
            log.debug("找到imgUrl:{} 准备替换为wxUrl:{}", url, wxUrl);
            replaceUrlMap.put(url, wxUrl);
        }
        // html 图片url转换
        replaceUrlMap.forEach((k, v) -> {
            wxDraft[0] = wxDraft[0].replaceAll(k, v);
        });
        return wxDraft[0];
    }

    /**
     * 添加草稿
     *
     * @param title 标题
     * @param author 作者
     * @param wxContent 微信草稿内容
     * @param ywUrl 原文url
     * @param mediaId 封面图片永久id
     * @return 草稿的media_id
     */
    public String addDraft(String title, String author, String wxContent, String ywUrl,
        String mediaId) throws Exception {
// POST（请使用 https 协议）https://api.weixin.qq.com/cgi-bin/draft/add?access_token=ACCESS_TOKEN
//        {
//            "articles": [
//            {
//                "title":TITLE,
//                "author":AUTHOR,
//                "digest":DIGEST,
//                "content":CONTENT,
//                "content_source_url":CONTENT_SOURCE_URL,
//                "thumb_media_id":THUMB_MEDIA_ID,
//                "need_open_comment":0,
//                "only_fans_can_comment":0
//            }
//            //若新增的是多图文素材，则此处应还有几段 articles 结构
//           ]
//        }
        String url = "https://api.weixin.qq.com/cgi-bin/draft/add?access_token=" + getToken();
        JSONObject content = new JSONObject();
        content.put("title", title);
        if (author != null) {
            content.put("author", author);
        }
//        content.put("digest",digest); 图文摘要
        content.put("content", wxContent);
        if (ywUrl != null) {
            content.put("content_source_url", ywUrl);
        }
        content.put("thumb_media_id", mediaId);
        // 0不开启评论 1开启评论（需要公众号有评论功能）
        content.put("need_open_comment", 1);
        content.put("only_fans_can_comment", 0);

        JSONArray array = new JSONArray();
        array.put(content);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("articles", array);

        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), jsonObject.toString());
        Request request = new Request.Builder().url(url).post(body).build();

        Response response = httpClient.newCall(request).execute();
        JSONObject responseJson = getResponseJson(response, "保存微信草稿失败");

        return responseJson.getString("media_id");
    }

    /**
     * 上传永久素材至微信
     *
     * @param materialType 素材类型
     * @param filePath 文件本地路径
     * @param videoInfo 视频info（如果素材类型是video）,title及introduction
     * @return []: 0-media_id  1-url
     * @throws Exception 异常
     */
    public String[] addMaterial(MaterialType materialType, String filePath, String... videoInfo)
        throws Exception {
        // https https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=ACCESS_TOKEN&type=TYPE
        String url =
            "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=" + getToken()
                + "&type=";
        switch (materialType) {
            case image:
                url += "image";
                break;
            case video:
                url += "video";
                break;
            case voice:
                url += "voice";
                break;
            case thumb:
                url += "thumb";
                break;
        }

        File file = new File(filePath);
        MediaType type = MediaType.parse("application/octet-stream");//"text/xml;charset=utf-8"
        RequestBody fileBody = RequestBody.create(type, file);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM)
            .addFormDataPart("media", file.getName(), fileBody);
        if (materialType == MaterialType.video) {
//            {
//                "title":VIDEO_TITLE,
//                "introduction":INTRODUCTION
//            }
            if (videoInfo.length == 2) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", videoInfo[0]);
                jsonObject.put("introduction", videoInfo[1]);
                builder.addFormDataPart("description", jsonObject.toString());
            } else {
                throw new Exception(
                    "materialType为video时,需传递videoInfo,长度为2的数组(title及introduction)");
            }
        }
        RequestBody multipartBody = builder.build();
        Request request = new Request.Builder()
            .url(url)
            .post(multipartBody)
            .build();
        Response response = httpClient.newCall(request).execute();
        JSONObject responseJson = getResponseJson(response, "上传素材至微信失败");

        final String[] mediaInfo = new String[2];
        mediaInfo[0] = responseJson.getString("media_id");
        mediaInfo[1] = responseJson.getString("url");

        return mediaInfo;
    }

    /**
     * 申请将草稿发布（不一定发布）
     *
     * @param draftId 草稿media_id
     * @return 是否提交成功
     */
    public boolean freePublishDraft(String draftId) throws Exception {
        // POST（请使用 https 协议）https://api.weixin.qq.com/cgi-bin/freepublish/submit?access_token=ACCESS_TOKEN
        String url =
            "https://api.weixin.qq.com/cgi-bin/freepublish/submit?access_token=" + getToken();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("media_id", draftId);
        RequestBody body = RequestBody.create(
            MediaType.parse("application/json"), jsonObject.toString());
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        Response response = httpClient.newCall(request).execute();
        getResponseJson(response, "草稿发布失败");
        return true;
    }

}

