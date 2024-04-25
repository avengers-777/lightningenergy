package com.alameda.lightningenergy.utils;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.bouncycastle.util.encoders.Hex;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.key.KeyPair;
import org.tron.trident.crypto.SECP256K1;
import org.tron.trident.crypto.tuwenitypes.Bytes;
import org.tron.trident.crypto.tuwenitypes.Bytes32;
import org.tron.trident.utils.Base58Check;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

public class SignatureRecovery {

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    public static String recoverAddressFromSignature(byte[] txid, byte[] signature) throws Exception {
        // 签名是 R + S + V 格式，需要将其分解
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(signature, 0, 32));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, 32, 64));
        int v = signature[64];

        if (v < 27) v += 27;

        // 检查 V 值的有效性
        if (v != 27 && v != 28) {
            throw new IllegalArgumentException("Invalid signature 'V' value");
        }

        BigInteger publicKey = recoverFromSignature((v - 27), r, s, txid);
        if (publicKey == null) {
            throw new IllegalArgumentException("Could not recover public key from signature");
        }

        // 计算地址
        return getAddress(publicKey);
    }

    private static BigInteger recoverFromSignature(int recId, BigInteger r, BigInteger s, byte[] message) {
        BigInteger n = CURVE.getN();  // Curve order.
        BigInteger i = BigInteger.valueOf((long) recId / 2);
        BigInteger x = r.add(i.multiply(n));
        // 椭圆曲线
        ECPoint R = decompressKey(x, (recId & 1) == 1);
        // 如果 R 无效，返回 null
        if (!R.multiply(n).isInfinity()) {
            return null;
        }
        BigInteger e = new BigInteger(1, message);
        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
        BigInteger rInv = r.modInverse(n);
        BigInteger srInv = rInv.multiply(s).mod(n);
        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
        ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE_PARAMS.getG(), eInvrInv, R, srInv);
        byte[] qBytes = q.getEncoded(false);
        return new BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.length));
    }

    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9 = new X9IntegerConverter();
        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(compEnc);
    }

    private static String getAddress(BigInteger publicKey) {
        // 将 BigInteger 转换为 SECP256K1.PublicKey 对象
        SECP256K1.PublicKey pubKey = SECP256K1.PublicKey.create(publicKey);

        // 使用 KeyPair 类的方法将公钥转换为 Base58Check 地址
        return KeyPair.publicKeyToBase58CheckAddress(pubKey);
    }

    public static void main(String[] args) throws Exception {
        byte[] txid = Hex.decode("0a0256bb220820d9060c7898964d40f0e6bbd7c6315a9b02082e1296020a3c747970652e676f6f676c65617069732e636f6d2f70726f746f636f6c2e4163636f756e745065726d697373696f6e557064617465436f6e747261637412d5010a154173417b95919fe43449e0695df554d21ece3fe7b312241a056f776e657220013a190a154173417b95919fe43449e0695df554d21ece3fe7b31001224b080210021a06616374697665200132207fff1fc0033ec30f0000000000000000000000000000000000000000000000003a190a154173417b95919fe43449e0695df554d21ece3fe7b310012249080210031a04746573742001322000000000000000060000000000000000000000000000000000000000000000003a190a15412e9ffa1e665fcbc2002b12e20f024bcc37ee851f1001709092b8d7c631");
        byte[] signature = Hex.decode("8ed7de6163764c763c45a2ca5efb03c1f081b6dcadce01339ef3cff4478411fe71320d376dae25dd122c0976ce3f9590dab9d5b0dc2cb7a5bfe9a4008ad5ff2a1c");

        // 使用 txid 和 signature 恢复地址
        String address = recoverAddressFromSignature(txid, signature);
        System.out.println("Recovered address: " + address);
    }

}

