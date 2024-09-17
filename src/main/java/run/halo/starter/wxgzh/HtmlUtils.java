package run.halo.starter.wxgzh;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    public static List<String> getImgUrls(String html) {
        // 正则匹配html或markdown中的图片链接
        String regex = "(src=|]\\()\"(.*?)\"";
        List<String> list = new ArrayList<>();

        Pattern pa = Pattern.compile(regex);
        Matcher ma = pa.matcher(html);
        while (ma.find()) {
            String src = ma.group();
            String regex1 = "http[s]?(.*?)(.jpg|.jpeg|.png|.gif)";
            Pattern pa1 = Pattern.compile(regex1, Pattern.DOTALL);
            Matcher ma1 = pa1.matcher(src);
            while (ma1.find()) {
                String group = ma1.group();
                if (list.contains(group)) {
                    continue;
                }
                list.add(group);
            }
        }
        return list;
    }

    public static String downFile(String urlString, String savePath,String filename) throws Exception {
        // url中文转码
        String urlEncode = urlEncodeChinese(urlString);
        // 构造URL
        URL url = new URL(urlEncode);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为20s
        con.setConnectTimeout(20 * 1000);
        //文件路径不存在 则创建
        File sf = new File(savePath);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        String filePath;
        if (filename == null) {
            String[] split = url.getPath().split("!");
            String[] split1 = split[0].split("/");
            filePath = sf.getPath() + "\\" + split1[split1.length - 1];
        } else {
            filePath = sf.getPath() + "\\" + filename;
        }

        //jdk 1.7 新特性自动关闭
        InputStream in = con.getInputStream();
        OutputStream out = Files.newOutputStream(Paths.get(filePath));
        //创建缓冲区
        byte[] buff = new byte[1024];
        int n;
        // 开始读取
        while ((n = in.read(buff)) >= 0) {
            out.write(buff, 0, n);
        }
        out.close();
        return filePath;
    }

    /**
     * url中的中文转码
     * @param url 请求url
     * @return 转码后的url
     */
    public static String urlEncodeChinese(String url) {
        Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url);
        String tmp = "";
        while (matcher.find()) {
            tmp = matcher.group();
            url = url.replaceAll(tmp, URLEncoder.encode(tmp, StandardCharsets.UTF_8));
        }
        return url.replace(" ", "%20");
    }


}

