package com.yqw.my.zuul.filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.yqw.my.zuul.http.RequestContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;



/**
 * 这个是PreFilter,前置执行过滤器，负责封装请求。步骤如下所示
 (1)封装请求头
 (2)封装请求体
 (3)构造出RestTemplate能识别的RequestEntity
 (4)将RequestEntity放入全局threadlocal之中
 */
public class RequestWrapperFilter extends EatuulFilter{

    @Override
    public String filterType() {
        // TODO Auto-generated method stub
        return "pre";
    }

    @Override
    public int filterOrder() {
        // TODO Auto-generated method stub
        return -1;
    }

    @Override
    public void run() {
        String rootURL = "http://localhost:9090";
        RequestContext ctx =RequestContext.getCurrentContext();
        HttpServletRequest servletRequest = ctx.getRequest();
        String targetURL = rootURL + servletRequest.getRequestURI();
        RequestEntity<byte[]> requestEntity = null;
        try {
            requestEntity = createRequestEntity(servletRequest, targetURL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //4、将requestEntity放入全局threadlocal之中
        ctx.setRequestEntity(requestEntity);
    }
    
    private RequestEntity createRequestEntity(HttpServletRequest request,String url) throws URISyntaxException, IOException {
        String method = request.getMethod();
        HttpMethod httpMethod = HttpMethod.resolve(method);
        //1、封装请求头
        MultiValueMap<String, String> headers =createRequestHeaders(request);
        //2、封装请求体
        byte[] body = createRequestBody(request);
        //3、构造出RestTemplate能识别的RequestEntity
        RequestEntity requestEntity = new RequestEntity<byte[]>(body,headers,httpMethod, new URI(url));
        return requestEntity;
    }
    

    private byte[] createRequestBody(HttpServletRequest request) throws IOException {
        InputStream inputStream = request.getInputStream();
        return StreamUtils.copyToByteArray(inputStream);
    }

    private MultiValueMap<String, String> createRequestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        List<String> headerNames = Collections.list(request.getHeaderNames());
        for(String headerName:headerNames) {
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            for(String headerValue:headerValues) {
                headers.add(headerName, headerValue);
            }
        }
        return headers;
    }
}