package com.alameda.lightningenergy.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Transient;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Signature {
    private SignatureType type;
    private String signature;
    private String message;
    @Transient
    @JsonIgnore
    private String[] messageArray;
    @AllArgsConstructor
    @Getter
    public enum SignatureType {
        LOG_IN("Please sign to let us verify that you are the owner of this address");
        private final String request;
    }
    private String[] toMessageArray() {
        if (this.messageArray == null) {
            this.messageArray = message.split("_");
        }
        return this.messageArray;
    }
    public Boolean validateMessageType() {
        return this.type.request.equals(getMessageTip());
    }
    public String getMessageTip(){
        String[] msgList = toMessageArray();
        if (msgList.length > 0) {
            return msgList[0];
        } else {
            return "";
        }
    }

    public String getAddress() {
        String[] msgList = toMessageArray();
        if (msgList.length > 1) {
            return msgList[1];
        } else {

            return "";
        }
    }

    public String getNonce() {
        String[] msgList = toMessageArray();
        if (msgList.length > 2) {
            return msgList[2];
        } else {
            return "";
        }
    }

}
