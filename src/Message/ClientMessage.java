/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.Serializable;

/**
 *
 * @author hoang
 */
public class ClientMessage implements Serializable{
    public enum REQUEST
    {
        LOGIN,
        LOGOUT,
        TABLEDATA;
    }
    private REQUEST request;
    private Object data;

    public ClientMessage(REQUEST request, Object data) {
        this.request = request;
        this.data = data;
    }

    public REQUEST getRequest() {
        return request;
    }

    public Object getData() {
        return data;
    }

    public void setRequest(REQUEST request) {
        this.request = request;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ClientMessage() {
    }
    
}
