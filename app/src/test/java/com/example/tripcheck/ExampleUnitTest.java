package com.example.tripcheck;

import com.example.tripcheck.bean.MessageBean;
import com.example.tripcheck.bean.MessageListBean;
import com.example.tripcheck.common.Constants;
import com.example.tripcheck.common.GlobalValue;
import com.example.tripcheck.http.HttpCallback;
import com.example.tripcheck.http.HttpManager;
import com.example.tripcheck.req.HttpRequest;
import com.example.tripcheck.utils.ByteUtils;
import com.example.tripcheck.utils.ToastUtils;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        //assertEquals(4, 2 + 2);
        double a=33.3333,b=66.66666;
        String str="longitude="+ a+"&latitude=" +b;
        //String state=str.substring(str.indexOf("state=")+"state=".length(),str.indexOf("&"));
        String longitude=str.substring(str.indexOf("longitude=")+"longitude=".length(),str.indexOf("&"));
        String latitude=str.substring(str.indexOf("latitude=")+"latitude=".length(),str.length());
        System.out.println(latitude);
        //System.out.println(str);
        //assertEquals("com.example.shendu", appContext.getPackageName());

        HttpManager.getInstance().addToken2Header(Constants.HTTP_TOKEN);
        HttpRequest.getChatHisData(1, GlobalValue.orgId, "48761571975421733375", new HttpCallback<MessageListBean>() {
            @Override
            public void onSuccess(MessageListBean data) {
                if (data != null) {
                    for (MessageBean item : data.getItems()) {
                        item.setName("old");
                        item.setHex_packet(item.getHex_packet().replace("48761571975421733375", ""));
                        item.setNormalData(item.getHex_packet());
                        item.setHexData(ByteUtils.str2HexStr(item.getHex_packet()));
                    }
                }
            }

            @Override
            public void onError(int code, String errMsg) {
                ToastUtils.showShort(errMsg);
            }

            @Override
            public void onFinish() {
            }
        });

    }
}