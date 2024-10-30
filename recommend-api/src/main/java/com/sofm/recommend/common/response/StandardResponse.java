package com.sofm.recommend.common.response;

import com.sofm.recommend.common.status.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse {
    private int status;
    private String message;
    private String recommend_id;
    private Object data;

    public StandardResponse(int status, String message, String recommend_id, Object data) {
        this.status = status;
        this.message = message;
        this.recommend_id = recommend_id;
        this.data = data;
    }

    public StandardResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static StandardResponse success(String recommend_id, Object data) {
        return new StandardResponse(ResponseStatus.OK, null, recommend_id, data);
    }


    public static StandardResponse success(Object data) {
        return new StandardResponse(ResponseStatus.OK, null, data);
    }

    public static StandardResponse error(int status, String message) {
        return new StandardResponse(status, message, null);
    }

    public static StandardResponse error(String message) {
        return new StandardResponse(ResponseStatus.INTERNAL_SERVER_ERROR, message, null);
    }
}
