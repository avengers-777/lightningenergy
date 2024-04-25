package com.alameda.lightningenergy.entity.common;

import com.alameda.lightningenergy.entity.enums.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResModel<T> {
    private Integer code;
    private String msg;
    private T data;
    private Long count;

    public static <T> ResModel<T> success(T object, Long count){
        ResModel<T> resModel = new ResModel<>();
        resModel.data = object;
        resModel.code = 0;
        resModel.count = count;
        resModel.msg = "Successful";
        return resModel;
    }

    public static <T> ResModel<T> success(T object){
        ResModel<T> resModel = new ResModel<>();
        resModel.data = object;
        resModel.code = 0;
        resModel.msg = "Successful";
        return resModel;
    }

    public static <T> ResModel<T> success(){
        ResModel<T> resModel = new ResModel<>();
        resModel.data = null;
        resModel.code = 0;
        resModel.msg = "Successful";
        return resModel;
    }

    public ResModel(Integer code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public static <T> ResModel<T> error(Integer code, String msg){
        ResModel<T> r = new ResModel<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
    public static <T> ResModel<T> error(ErrorType.ApplicationException e){
        ResModel<T> r = new ResModel<>();
        r.setCode(e.getCode());
        r.setMsg(e.getMessage());
        return r;
    }

    public static <T> ResModel<T> errorNoMono(ErrorType errorType){
        ResModel<T> resModel = new ResModel<>();
        resModel.setCode(errorType.getCode());
        resModel.setMsg(errorType.getMsg());
        return resModel;
    }

    public static  <T> ResModel<T> error(ErrorType errorType){
        ResModel<T> resModel = new ResModel<>();
        resModel.setCode(errorType.getCode());
        resModel.setMsg(errorType.getMsg());
        return resModel;
    }
}
