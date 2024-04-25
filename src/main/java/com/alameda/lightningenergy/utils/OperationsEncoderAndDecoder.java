package com.alameda.lightningenergy.utils;


import com.alameda.lightningenergy.entity.enums.ContractType;
import com.google.protobuf.ByteString;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.proto.Chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class  OperationsEncoderAndDecoder{

    // Description: get operations code according to the input contract types
    public static String operationsEncoder(ContractType[] contractId){

        List<ContractType> list = new ArrayList<>(Arrays.asList(contractId));
        byte[] operations = new byte[32];
        list.forEach(e -> {
            int num = e.getNum();
            operations[num / 8] |= (1 << num % 8);
        });

        return Hex.toHexString(operations);
    }

    // Description: get all allowable contract types according to the operations code
    public static HashSet<ContractType> operationsDecoder(ByteString operations){
        String items =  Hex.toHexString(operations.toByteArray());
        List<ContractType> contractIDs = new ArrayList<>();
        byte[] opArray = Hex.decode(items);
        for(int i=0;i<32;i++) // 32 bytes
        {
            for(int j=0;j<8;j++)
            {
                if((opArray[i]>>j & 0x1) ==1) {
                    contractIDs.add(ContractType.getContractTypeByNum(i*8+j));
                }
            }
        }
        return new HashSet<>(contractIDs);
    }


}