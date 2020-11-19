/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Model.Message;

import java.io.Serializable;

/**
 *
 * @author hoang
 */
public class ServerMessage implements Serializable{
    public enum STATUS 
    {
        S_OK,
        S_FAIL,
        S_WARN;
    }
    public enum ACTION
    {
        NONE,
        MESSAGE_BOX;
    }
    public enum REQUEST
    {
        LOGIN,
        CHALLENGE,
        TABLEDATA;
    }
    private STATUS status;
    private ACTION action;
    private Object data;
    private REQUEST request;
    public STATUS getStatus() {
        return status;
    }

    public ACTION getAction() {
        return action;
    }

    public Object getData() {
        return data;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setAction(ACTION action) {
        this.action = action;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public REQUEST getRequest() {
        return request;
    }

    public void setRequest(REQUEST request) {
        this.request = request;
    }

    public ServerMessage(STATUS status, ACTION action, Object data, REQUEST request) {
        this.status = status;
        this.action = action;
        this.data = data;
        this.request = request;
    }

    public ServerMessage()
    {
        
    }
}
