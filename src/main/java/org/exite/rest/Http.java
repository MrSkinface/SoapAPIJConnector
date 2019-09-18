package org.exite.rest;

import java.nio.charset.StandardCharsets;

import lombok.extern.slf4j.Slf4j;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpHost;

import static org.exite.utils.JsonUtils.toJson;
import static org.exite.utils.JsonUtils.fromJson;

@Slf4j
public final class Http {

    private static HttpHost proxy;

    static{
        if(System.getProperty("proxyHost") != null && System.getProperty("proxyPort") != null)
            proxy = new HttpHost(System.getProperty("proxyHost"), Integer.parseInt(System.getProperty("proxyPort")));
    }

	public static Object post(final String address, final Object obj, final Class<? extends Object> c) {
		try {
		    if(proxy != null)
                Unirest.setProxy(proxy);
			HttpResponse<JsonNode> jsonResponse;
			jsonResponse = Unirest
					.post(address)
					.header("accept", "application/json")
					.body(toJson(obj).getBytes(StandardCharsets.UTF_8))
					.asJson();
			debug(address, obj, jsonResponse);
			return fromJson(jsonResponse.getBody().toString(), c);
		} catch (UnirestException e) {
            log.error(e.getMessage(), e);
		}
		return null;
	}

	private static final void debug(final String address, final Object obj, final HttpResponse response){
	    if(response.getStatus() != 200){
            log.debug(address + " --> " + toJson(obj));
            log.debug(response.getBody().toString());
        }
    }
}