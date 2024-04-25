package com.alameda.lightningenergy.utils;


import com.google.protobuf.ByteString;


import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigInteger;

import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.crypto.SECP256K1;
import org.tron.trident.crypto.SECP256K1.PublicKey;
import org.tron.trident.crypto.SECP256K1.Signature;
import org.tron.trident.crypto.tuwenitypes.Bytes;
import org.tron.trident.crypto.tuwenitypes.Bytes32;
import org.tron.trident.utils.Base58Check;

public class Utils {
    public static final Long millisecondsOfDay = 1000L * 60L * 60L * 24L;

    public static String getTronAddress(byte[] txid, byte[] signature) {
        SECP256K1.Signature sig = Signature.decode(Bytes.wrap(signature));
        SECP256K1.PublicKey pubKey = PublicKey.recoverFromSignature(Bytes32.wrap(txid), sig).get();
        byte[] addressFromPubKey = KeyPair.publicKeyToAddress(pubKey);
        return Base58Check.bytesToBase58(addressFromPubKey);
    }



    public static <M> void merge(M target,M destination) throws Exception{
        BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()){
            if(descriptor.getWriteMethod() != null){
                Object defaultValue = descriptor.getReadMethod().invoke(destination);
                if(defaultValue !=null && !"".equals(defaultValue)){
                    descriptor.getWriteMethod().invoke(target,defaultValue);
                }
            }

        }
    }
    public static BigInteger byteStringToBigInteger(ByteString byteString) {
        // 将ByteString转换为字节数组
        byte[] bytes = byteString.toByteArray();

        // 将字节数组转换为BigInteger
        BigInteger number = new BigInteger(bytes);

        return number;
    }
    public static String byteStringToBinary(ByteString byteString) {
        StringBuilder binaryString = new StringBuilder();
        for (byte b : byteString.toByteArray()) {
            String binaryByte = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            binaryString.append(binaryByte);
        }
        return binaryString.toString();
    }
    public static String byteStringToHex(ByteString byteString) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteString.toByteArray()) {
            String hexByte = String.format("%02x", b);
            hexString.append(hexByte);
        }
        return hexString.toString();
    }




};
