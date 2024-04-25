package com.alameda.lightningenergy.entity.data;

import com.alameda.lightningenergy.entity.dto.AdminRequest;
import com.alameda.lightningenergy.entity.enums.AdminPermission;
import com.alameda.lightningenergy.entity.enums.ErrorType;
import com.alameda.lightningenergy.utils.SignatureVerification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.web3j.crypto.Keys;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("administrators")
@Builder
public class Admin {
    @Id
    private String id;
    private String name;
    @Indexed
    private String ethereumAddress;
    private Set<AdminPermission> permissions;
    private Long updateDate;
    private Long createDate;
    private String nonce;
    private Status status;

    public Admin(AdminRequest request){
        String checksumAddress = Keys.toChecksumAddress(request.getEthereumAddress());
        this.name = request.getName();
        this.ethereumAddress = checksumAddress;
        this.permissions = request.getPermissions();
        this.updateDate = System.currentTimeMillis();
        this.createDate = System.currentTimeMillis();
        this.nonce = UUID.randomUUID().toString();
        this.status = request.getStatus();
    }

    public enum Status {
        ACTIVE, // 活跃
        DELETED,
        DISABLED, // 禁用
        PENDING // 待审核
    }
    public Mono<Admin> verifyLoginSignature(Signature signature){
        // Please sign to let us verify that you are the owner of this address_0xdd2EC2d98bb7caBE4468039BB1Ad28Dd3403ea8E_f9f320da-2cb1-495a-b9f1-c0c55ad4bab2

        return Mono.just(this)
                .filter(admin -> signature.validateMessageType())
                .switchIfEmpty(Mono.error(ErrorType.WRONG_SIGNATURE_TYPE.getException()))
                .filter(admin -> admin.ethereumAddress.equals(signature.getAddress()))
                .switchIfEmpty(Mono.error(ErrorType.ADDRESS_VERIFICATION_ERROR.getException()))
                .filter(admin -> admin.nonce.equals(signature.getNonce()))
                .switchIfEmpty(Mono.error(ErrorType.NONCE_ERROR.getException()))
                .filter(admin -> admin.status.equals(Status.ACTIVE))
                .switchIfEmpty(Mono.error(ErrorType.ABNORMAL_STATUS.getException()))
                .filter(admin -> SignatureVerification.validate(signature.getSignature(),signature.getMessage(),ethereumAddress))
                .switchIfEmpty(Mono.error(ErrorType.SIGNATURE_VERIFICATION_FAILED.getException()))
                .map(Admin::updateNonce);



    }
    private Admin updateNonce(){
        this.nonce = UUID.randomUUID().toString();
        this.updateDate = System.currentTimeMillis();
        return this;
    }


}
