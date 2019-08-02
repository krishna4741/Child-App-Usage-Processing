package com.optiquall.childappusage.data;

public class Request {
    private String from_parent;
    private String to_child;
    private String req_status;

    public Request(String from_parent, String to_child, String req_status) {
        this.from_parent = from_parent;
        this.to_child = to_child;
        this.req_status = req_status;
    }

    public Request() {

    }


    public String getFrom_parent() {
        return from_parent;
    }

    public void setFrom_parent(String from_parent) {
        this.from_parent = from_parent;
    }

    public String getTo_child() {
        return to_child;
    }

    public void setTo_child(String to_child) {
        this.to_child = to_child;
    }

    public String getReq_status() {
        return req_status;
    }

    public void setReq_status(String req_status) {
        this.req_status = req_status;
    }
}
