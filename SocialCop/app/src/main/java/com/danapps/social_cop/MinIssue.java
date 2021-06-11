package com.danapps.social_cop;


public class MinIssue {
    private String proof, desc, from, locality;
    private int status;


    public MinIssue() {
    }

    public MinIssue(String proof, String desc, String from, String locality, int status) {
        this.proof = proof;
        this.desc = desc;
        this.from = from;
        this.locality = locality;
        this.status = status;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
