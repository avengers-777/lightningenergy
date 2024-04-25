package com.alameda.lightningenergy.config.security;

import cn.dev33.satoken.exception.DisableServiceException;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.alameda.lightningenergy.entity.common.ResModel;
import com.alameda.lightningenergy.entity.enums.ErrorType;

public class SaTokenGlobalExceptionHandler {

    static public ResModel<Object> handlerSaTokenException(Throwable e) {

        switch (e) {
            case NotLoginException ee -> {
                return ResModel.error(ErrorType.NOT_LOGGED_IN_EXCEPTION);
            }
            case NotRoleException ee -> {
                return ResModel.error(ErrorType.THE_CHARACTER_IS_ABNORMAL);
            }
            case NotPermissionException ee -> {
                return   ResModel.error(ErrorType.PERMISSION_EXCEPTION);
            }
            case DisableServiceException ee -> {
                return ResModel.error(ErrorType.ACCOUNT_BANNED.getCode(),"Account banned: " + ee.getDisableTime() + " Unblock in seconds");
            }
            case null -> {
                return ResModel.error(ErrorType.ERROR_INTERSECTIONALITIES);
            }
            default -> {
                return ResModel.error(ErrorType.UNKNOWN_ERROR.getCode(),e.getMessage());
            }

        }

    }
}
