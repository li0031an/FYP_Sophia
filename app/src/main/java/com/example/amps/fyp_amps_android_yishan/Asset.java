package com.example.amps.fyp_amps_android_yishan;

public class Asset {
    String asset_id;
    String ext = "";
    String name;
    String estimated_datestart;
    String estimated_dateend;
    String actual_datestart;
    String actual_dateend;
    String tags;
    String trash;
    String base64_thumbnail;
    //No getter and setter for videoUrl
    String videoUrl;
    String created_userid;
    String created_username;
    String created_datetime;
    String updated_userid;
    String updated_username;
    String updated_datetime;
//    String revNum;
    String revId;

    String tracking_status;
    String userid;
    String statusid;
    String stepid;

    String latest_revid;
    String latest_revnum;
    double latest_revsize;
    double file_size;
    String assigned_userid;
    String workflow_step_id;

    FileType fileType;

    public enum FileType {
        IMAGE,
        VIDEO,
        AUDIO,
        DOCUMENT,
        OTHER
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setCreated_username(String created_username) {
        this.created_username = created_username;
    }

    public void setUpdated_username(String updated_username) {
        this.updated_username = updated_username;
    }

    public String getCreated_username() {
        return created_username;
    }

    public String getUpdated_username() {
        return updated_username;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getLatest_revid() {
        return latest_revid;
    }

    public void setLatest_revid(String latest_revid) {
        this.latest_revid = latest_revid;
    }

    public double getLatest_revsize() {
        return latest_revsize;
    }

    public void setLatest_revsize(double latest_revsize) {
        this.latest_revsize = latest_revsize;
    }

    public String getLatest_revnum() {
        return latest_revnum;
    }

    public void setLatest_revnum(String latest_revnum) {
        this.latest_revnum = latest_revnum;
    }

    public String getAsset_id() {
        return asset_id;
    }

    public void setAsset_id(String asset_id) {
        this.asset_id = asset_id;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEstimated_datestart() {
        return estimated_datestart;
    }

    public void setEstimated_datestart(String estimated_datestart) {
        this.estimated_datestart = estimated_datestart;
    }

    public String getEstimated_dateend() {
        return estimated_dateend;
    }

    public void setEstimated_dateend(String estimated_dateend) {
        this.estimated_dateend = estimated_dateend;
    }

    public String getActual_datestart() {
        return actual_datestart;
    }

    public void setActual_datestart(String actual_datestart) {
        this.actual_datestart = actual_datestart;
    }

    public String getActual_dateend() {
        return actual_dateend;
    }

    public void setActual_dateend(String actual_dateend) {
        this.actual_dateend = actual_dateend;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTrash() {
        return trash;
    }

    public void setTrash(String trash) {
        this.trash = trash;
    }

    public String getBase64_thumbnail() {
        return base64_thumbnail;
    }

    public void setBase64_thumbnail(String base64_thumbnail) {
        this.base64_thumbnail = base64_thumbnail;
    }

    public String getCreated_userid() {
        return created_userid;
    }

    public void setCreated_userid(String created_userid) {
        this.created_userid = created_userid;
    }

    public String getCreated_datetime() {
        return created_datetime;
    }

    public void setCreated_datetime(String created_datetime) {
        this.created_datetime = created_datetime;
    }

    public String getUpdated_userid() {
        return updated_userid;
    }

    public void setUpdated_userid(String updated_userid) {
        this.updated_userid = updated_userid;
    }

    public String getUpdated_datetime() {
        return updated_datetime;
    }

    public void setUpdated_datetime(String updated_datetime) {
        this.updated_datetime = updated_datetime;
    }

    public String getTracking_status() {
        return tracking_status;
    }

    public void setTracking_status(String tracking_status) {
        this.tracking_status = tracking_status;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getStatusid() {
        return statusid;
    }

    public void setStatusid(String statusid) {
        this.statusid = statusid;
    }

    public String getStepid() {
        return stepid;
    }

    public void setStepid(String stepid) {
        this.stepid = stepid;
    }

    public double getFile_size() {
        return file_size;
    }

    public void setFile_size(double file_size) {
        this.file_size = file_size;
    }

    public String getAssigned_userid() {
        return assigned_userid;
    }

    public void setAssigned_userid(String assigned_userid) {
        this.assigned_userid = assigned_userid;
    }

    public String getWorkflow_step_id() {
        return workflow_step_id;
    }

    public void setWorkflow_step_id(String workflow_step_id) {
        this.workflow_step_id = workflow_step_id;
    }

    public void setRevId(String revId) {
        this.revId = revId;
    }

    public String getRevId() {
        return revId;
    }

//    public String getRevNum() {
//        return revNum;
//    }
//
//    public void setRevNum(String revNum) {
//        this.revNum = revNum;
//    }

}
