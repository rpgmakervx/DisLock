package org.easyarch.dislock.kits;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by xingtianyu(code4j) on 2017-7-10.
 */
public class JsonKits {

    public static String toString(Map<String,Object> jsonMap){
        return JSONObject.toJSONString(jsonMap,SerializerFeature.DisableCircularReferenceDetect);
    }

    public static <T>String toString(T t){
        return JSONObject.toJSONString(t,true);
    }

    public static String toString(List list){
        return JSON.toJSONString(list,SerializerFeature.DisableCircularReferenceDetect);
    }

    public static <T> T toObject(String json,Class<T> cls){
        if (StringUtils.isBlank(json))
            return null;
        return JSONObject.parseObject(json,cls);
    }

    public static <T> List<T> toList(String json,Class<T> cls){
        if (StringUtils.isBlank(json))
            return null;
        return JSONArray.parseArray(json,cls);
    }

}
