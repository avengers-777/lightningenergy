package com.alameda.lightningenergy.entity.dto;

import com.alameda.lightningenergy.entity.data.Admin;
import com.alameda.lightningenergy.entity.enums.AdminPermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.web3j.crypto.Keys;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminRequest {
    private String name;
    private String ethereumAddress;
    private Set<AdminPermission> permissions;
    private Admin.Status status;


    public String checksumAddress(){

        return Keys.toChecksumAddress(this.getEthereumAddress());
    }
}
