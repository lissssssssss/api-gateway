/* Copyright 2012 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.membrane.core.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.predic8.membrane.core.exchange.Exchange;
import com.predic8.membrane.core.http.AbstractBody;
import com.predic8.membrane.core.http.Body;

import java.io.IOException;

public class MyInterceptor extends AbstractInterceptor {

    @Override
    public Outcome handleRequest(Exchange exc) {
        System.out.println("MyInterceptor maven at request invoked.");
        return Outcome.CONTINUE;
    }

    @Override
    public Outcome handleResponse(Exchange exc) {
        System.out.println("MyInterceptor maven at response invoked.");
        AbstractBody body = exc.getResponse().getBody();
        
        try {
            // 检查响应是否包含JSON内容
            if (body == null || !exc.getResponse().getHeader().getContentType().contains("application/json")) {
                return Outcome.CONTINUE;
            }
            
            // 读取并解析JSON响应
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(body.getContent());
            
            // 检查是否为对象类型
            if (!rootNode.isObject()) {
                return Outcome.CONTINUE;
            }
            
            ObjectNode objectNode = (ObjectNode) rootNode;
            
            // 检查是否存在msg属性
            if (objectNode.has("msg")) {
                // 获取msg属性的值
                String msgValue = objectNode.get("msg").asText();
                
                // 删除msg属性
                objectNode.remove("msg");
                
                // 添加message属性，值为原msg的值
                objectNode.put("message", msgValue);
                
                // 将修改后的JSON写回响应体 - 修正了这里，使用getBytes()将String转换为byte数组
                String modifiedJson = mapper.writeValueAsString(objectNode);
                exc.getResponse().setBody(new Body(modifiedJson.getBytes()));
                exc.getResponse().getHeader().setContentLength(modifiedJson.length());
            }
            
        } catch (IOException e) {
            System.err.println("Error processing JSON response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Outcome.CONTINUE;
    }
}