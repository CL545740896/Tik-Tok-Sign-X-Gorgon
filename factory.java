

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.bytedance.frameworks.core.encrypt.TTEncryptUtils;
import com.google.gson.Gson;
import com.ss.sys.ces.a;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.util.MediaType;
import com.tiktok.opensdk.tiktokapi.DeviceUtil;
import com.tiktok.opensdk.tiktokapi.bean.DeviceBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

@RestController
public class RequestController {
    private static final String NULL_MD5_STRING = "00000000000000000000000000000000";
    public String sessionid = "";
    public String xtttoken = "";

    @PostMapping(value = "/request", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String test(@RequestBody String requestBodyString) {
        JSONObject jsonObject = new JSONObject();
        JSONObject requestBody = JSONObject.parseObject(requestBodyString);
        String ck = requestBody.getString("cookie");
        String url = requestBody.getString("url");
        String postData = requestBody.getString("postData");
        if (ck == null || url == null) {
            jsonObject.put("status_code", 0);
            jsonObject.put("status_msg", "");
            return jsonObject.toJSONString();
        }
        if (url.contains("douplus/order/create")) {
            JSONObject deviceData = getNewDeviceData();
            url = replaceUrlParam(url, "device_id", deviceData.getString("device_id"));
            url = replaceUrlParam(url, "iid", deviceData.getString("install_id"));
        }

        long time = System.currentTimeMillis() / 1000;
        String p = url.substring(url.indexOf("?") + 1, url.length());
        boolean isPost = postData != null && !postData.equals("");
        String result;
        if (isPost) {
            FormBody.Builder formBody = new FormBody.Builder();
            String STUB = encryption(postData);
            Map<String, String> map = new HashMap<>();
            String[] ks = postData.split("&");
            for (int i = 0; i < ks.length; i++) {
                String[] ur = ks[i].split("=");
                if (ur.length == 1) {
                    map.put(ur[0], "");
                } else {
                    map.put(ur[0], ur[1]);

                }
            }
            for (Map.Entry<String, String> m : map.entrySet()) {
                formBody.add(m.getKey(), m.getValue());
            }
            String s = getXGon(p, STUB, ck, sessionid);
            String XGon = ByteToStr(a.leviathan((int) time, StrToByte(s)));
            result = doPostNet(url, formBody.build(), time, XGon, STUB, ck);
        } else {
            String s = getXGon(p, "", ck, sessionid);
            String XGon = ByteToStr(a.leviathan((int) time, StrToByte(s)));
            result = doGetNet(url, time, XGon, ck);
        }
        jsonObject = JSONObject.parseObject(result);
        if (jsonObject == null) {
            jsonObject = new JSONObject();
            jsonObject.put("status_code", -2);
            jsonObject.put("status_msg", "");
        }
        return jsonObject.toJSONString();
    }


    @GetMapping(value = "/getDeviceData", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getDeviceData() {
        return getNewDeviceData().toJSONString();
    }

    @GetMapping(value = "/getQuery", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getQuery() {
        String uuid = DeviceUtil.getRanInt(15);  //
        String openudid = DeviceUtil.getRanInt(16);  //android_id
        String _rticket = System.currentTimeMillis() + "";   //
        String url = "https://api2-16-h2.musical.ly/service/2/device_register/?mcc_mnc=46000&ac=wifi&channel=aweGW&aid=1128&app_name=aweme&version_code=550&version_name=5.5.0&device_platform=android&ssmix=a&device_type=SM-G925F&device_brand=samsung&language=zh&os_api=22&os_version=5.1.1&uuid=" + uuid + "&openudid=" + openudid + "&manifest_version_code=550&resolution=720*1280&dpi=192&update_version_code=5502&_rticket=" + _rticket + "&tt_data=a&config_retry=b";
        String stb = url.substring(url.indexOf("?") + 1, url.length());
        String STUB = encryption(stb).toUpperCase();
        String ck = "odin_tt=9c1e0ebae55f3c2d9f71ab2aadce63126022e8960819bace07d441d977ad60eff6312161f546ebfe747528d03d53a161728250938c4287a588d86aa599c284b3; qh[360]=1; install_id=66715314288; ttreq=1$0b4589453328800ed93e002538883aa52da3e1d5";
        int time = (int) (System.currentTimeMillis() / 1000);
        String s = getXGon(url, STUB, ck, null);
        String XGon = ByteToStr(a.leviathan(time, StrToByte(s)));
        String device = getDevice(openudid, uuid);
        JSONObject deviceJson = JSONObject.parseObject(device);
        final okhttp3.RequestBody formBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/octet-stream;tt-data=a"), this.toGzip(device));
        String result = doPostNet(url, formBody, time, XGon, "", ck);
        JSONObject deviceResult = JSONObject.parseObject(result);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status_code", 0);
        JSONObject headerJson = deviceJson.getJSONObject("header");
        String data = String.format("os_api=22&device_type=SM-G925F&ssmix=a&manifest_version_code=911&dpi=320&uuid=%s&app_name=aweme&version_name=9.1.1&ts=%d&app_type=normal&ac=wifi&update_version_code=9104&channel=huawei_1&_rticket=%s&device_platform=android&iid=%s&version_code=911&cdid=%s&openudid=%s&device_id=%s&resolution=720*1280&os_version=5.1.1&language=zh&device_brand=OPPO&aid=1128&mcc_mnc=46007",
                uuid, time, _rticket, deviceResult.getString("install_id_str"), headerJson.getString("clientudid"), openudid, deviceResult.getString("device_id_str"));
        jsonObject.put("data", data);
        Log.i("data", data);
        return jsonObject.toJSONString();
    }

    public JSONObject getNewDeviceData() {
        String uuid = DeviceUtil.getRanInt(15);
        String openudid = DeviceUtil.getRanInt(16);  //android_id
        String _rticket = System.currentTimeMillis() + "";
        String url = "https://api2-16-h2.musical.ly/service/2/device_register/?mcc_mnc=46000&ac=wifi&channel=aweGW&aid=1128&app_name=aweme&version_code=550&version_name=5.5.0&device_platform=android&ssmix=a&device_type=SM-G925F&device_brand=samsung&language=zh&os_api=22&os_version=5.1.1&uuid=" + uuid + "&openudid=" + openudid + "&manifest_version_code=550&resolution=720*1280&dpi=192&update_version_code=5502&_rticket=" + _rticket + "&tt_data=a&config_retry=b";
        String stb = url.substring(url.indexOf("?") + 1, url.length());
        String STUB = encryption(stb).toUpperCase();
        String ck = "odin_tt=9c1e0ebae55f3c2d9f71ab2a12ce63c46022e8912819bace07d441d977ad60eff6301161f546ebfe747528d03d53a161728250938c4287a588d86aa599c284b3; qh[360]=1; install_id=66715314288; ttreq=1$0b4589453328800ed93e002538883aa52da3e1d5";
        int time = (int) (System.currentTimeMillis() / 1000);
        String s = getXGon(url, STUB, ck, null);
        String XGon = ByteToStr(a.leviathan(time, StrToByte(s)));
        final okhttp3.RequestBody formBody = okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/octet-stream;tt-data=a"), this.toGzip(this.getDevice(openudid, uuid)));
        String result = doPostNet(url, formBody, time, XGon, "", ck);
        JSONObject jsonObject = JSONObject.parseObject(result);
        if (jsonObject == null) {
            jsonObject = new JSONObject();
            jsonObject.put("status_code", -2);
            jsonObject.put("status_msg", "");
        }
        return jsonObject;
    }

    public String getDevice(String openudid, String udid) {

        String Serial_number = DeviceUtil.getRanInt(8);
        DeviceBean deviceBean = new DeviceBean();
        deviceBean.set_gen_time(System.currentTimeMillis() + "");
        deviceBean.setMagic_tag("ss_app_log");

        DeviceBean.HeaderBean headerBean = new DeviceBean.HeaderBean();
        headerBean.setDisplay_name("");
        headerBean.setUpdate_version_code(5502);
        headerBean.setManifest_version_code(550);
        headerBean.setAid(1128);
        headerBean.setChannel("aweGW");
        headerBean.setAppkey("59bfa27c67e59e7d920028d9"); //appkey
        headerBean.setPackageX("com.ss.android.ugc.aweme");
        headerBean.setApp_version("5.5.0");
        headerBean.setVersion_code(550);
        headerBean.setSdk_version("2.5.5.8");
        headerBean.setOs("Android");
        headerBean.setOs_version("5.1.1");
        headerBean.setOs_api(22);
        headerBean.setDevice_model("SM-G925F");
        headerBean.setDevice_brand("samsung");
        headerBean.setDevice_manufacturer("samsung");
        headerBean.setCpu_abi("armeabi-v7a");
        headerBean.setBuild_serial(Serial_number);  ////android.os.Build.SERIAL
        headerBean.setRelease_build("2132ca7_20190321");  // releas
        headerBean.setDensity_dpi(192);
        headerBean.setDisplay_density("mdpi");
        headerBean.setResolution("1280x720");
        headerBean.setLanguage("zh");
        headerBean.setMc(DeviceUtil.getMac());  //mac
        headerBean.setTimezone(8);
        headerBean.setAccess("wifi");
        headerBean.setNot_request_sender(0);
        headerBean.setCarrier("China Mobile GSM");
        headerBean.setMcc_mnc("46000");
        headerBean.setRom("eng.se.infra.20181117.120021");
        headerBean.setRom_version("samsung-user 5.1.1 20171130.276299 release-keys");
        headerBean.setSig_hash("aea615ab910015038f73c47e45d21466");
        headerBean.setDevice_id("");
        headerBean.setOpenudid(openudid);
        headerBean.setUdid(udid);
        headerBean.setClientudid(UUID.randomUUID().toString());
        headerBean.setSerial_number(Serial_number);
        headerBean.setRegion("CN");
        headerBean.setTz_name("Asia\\/Shanghai");
        headerBean.setTimezone(28800);
        headerBean.setSim_region("cn");
        List<DeviceBean.HeaderBean.SimSerialNumberBean> sim_serial_number = new ArrayList<>();
        DeviceBean.HeaderBean.SimSerialNumberBean bean = new DeviceBean.HeaderBean.SimSerialNumberBean();
        bean.setSim_serial_number(DeviceUtil.getRanInt(20));
        sim_serial_number.add(bean);
        headerBean.setSim_serial_number(sim_serial_number);
        deviceBean.setHeader(headerBean);
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        timeZone.getID();

        Gson gson = new Gson();
        return gson.toJson(deviceBean);
    }

    public byte[] toGzip(String r) {
        try {
            byte[] bArr2 = r.getBytes("UTF-8");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(8192);
            GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gZIPOutputStream.write(bArr2);
            gZIPOutputStream.close();
            bArr2 = byteArrayOutputStream.toByteArray();
            bArr2 = TTEncryptUtils.a(bArr2, bArr2.length);
            return bArr2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public class RetryIntercepter implements Interceptor {

        public int maxRetry;
        private int retryNum = 0;

        public RetryIntercepter(int maxRetry) {
            this.maxRetry = maxRetry;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            boolean isSuccessful;
            Response response = null;
            try {
                response = chain.proceed(request);
                isSuccessful = response.isSuccessful();
            } catch (Exception e) {
                isSuccessful = false;
            }
            while (!isSuccessful && retryNum < maxRetry) {
                retryNum++;
                System.out.println("retryNum=" + retryNum);
                response = chain.proceed(request);
            }

            return response;
        }
    }

    public String doGetNet(String url, long time, String XGon, String ck) {

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-SS-REQ-TICKET", System.currentTimeMillis() + "")
                .addHeader("X-Khronos", time + "")
                .addHeader("X-Gorgon", XGon)
                .addHeader("sdk-version", "1")
                .addHeader("Cookie", ck)
                .addHeader("X-Pods", "")
                .addHeader("Connection", "Keep-Alive")
                .addHeader("User-Agent", "okhttp/3.10.0.1")
                .addHeader("x-tt-token", xtttoken)
                .addHeader("Accept-Encoding", "identity")
                .addHeader("Connection", "Upgrade, HTTP2-Settings")
                .addHeader("Upgrade", "h2c")
                .build();
        List<Protocol> protocols = new ArrayList<>();
        protocols.add(Protocol.HTTP_2);
        protocols.add(Protocol.HTTP_1_1);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .protocols(protocols)
                .connectionPool(new ConnectionPool(10, 30, TimeUnit.SECONDS))
                .addInterceptor(new RetryIntercepter(3))
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.isSuccessful()) {
            try {
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status_code", -2);
        jsonObject.put("status_msg", "");
        return jsonObject.toJSONString();
    }

    public String doPostNet(String url, okhttp3.RequestBody requestBody, long time, String XGon, String stub, String ck) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("X-SS-STUB", stub)
                .addHeader("X-SS-REQ-TICKET", System.currentTimeMillis() + "")
                .addHeader("X-Khronos", time + "")
                .addHeader("X-Gorgon", XGon)
                .addHeader("sdk-version", "1")
                .addHeader("Cookie", ck)
                .addHeader("X-Pods", "")
                .addHeader("Connection", "Keep-Alive")
                .addHeader("User-Agent", "okhttp/3.10.0.1")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .addHeader("x-tt-token", xtttoken)
                .build();
        List<Protocol> protocols = new ArrayList<>();
        protocols.add(Protocol.HTTP_2);
        protocols.add(Protocol.HTTP_1_1);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .protocols(protocols)
                .connectionPool(new ConnectionPool(10, 30, TimeUnit.SECONDS))
                .addInterceptor(new RetryIntercepter(3))
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response.isSuccessful()) {
            try {
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status_code", -2);
        jsonObject.put("status_msg", "");
        return jsonObject.toJSONString();
    }

    public static byte[] StrToByte(String str) {
        String str2 = str;
        Object[] objArr = new Object[1];
        int i = 0;
        objArr[0] = str2;

        int length = str.length();
        byte[] bArr = new byte[(length / 2)];
        while (i < length) {
            bArr[i / 2] = (byte) ((Character.digit(str2.charAt(i), 16) << 4) + Character.digit(str2.charAt(i + 1), 16));
            i += 2;
        }
        return bArr;
    }

    public static String ByteToStr(byte[] bArr) {

        int i = 0;

        char[] toCharArray = "0123456789abcdef".toCharArray();
        char[] cArr = new char[(bArr.length * 2)];
        while (i < bArr.length) {
            int i2 = bArr[i] & 255;
            int i3 = i * 2;
            cArr[i3] = toCharArray[i2 >>> 4];
            cArr[i3 + 1] = toCharArray[i2 & 15];
            i++;
        }
        return new String(cArr);

    }

    public String encryption(String str) {
        String re_md5 = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return re_md5.toUpperCase();
    }

    public String getXGon(String url, String stub, String ck, String sessionid) {
        StringBuilder sb = new StringBuilder();
        if (TextUtils.isEmpty(url)) {
            sb.append(NULL_MD5_STRING);
        } else {
            sb.append(encryption(url).toLowerCase());
        }

        if (TextUtils.isEmpty(stub)) {
            sb.append(NULL_MD5_STRING);
        } else {
            sb.append(stub);
        }

        if (TextUtils.isEmpty(ck)) {
            sb.append(NULL_MD5_STRING);
        } else {
            sb.append(encryption(ck).toLowerCase());
        }

        if (TextUtils.isEmpty(sessionid)) {
            sb.append(NULL_MD5_STRING);
        } else {
            sb.append(encryption(sessionid).toLowerCase());
        }
        return sb.toString();
    }


    public static String replaceUrlParam(String url, String name, String value) {
        int index = url.indexOf(name + "=");
        if (index != -1) {
            StringBuilder sb = new StringBuilder();
            sb.append(url.substring(0, index)).append(name).append("=").append(value);
            int idx = url.indexOf("&", index);
            if (idx != -1) {
                sb.append(url.substring(idx));
            }
            url = sb.toString();
        }
        return url;
    }
}
