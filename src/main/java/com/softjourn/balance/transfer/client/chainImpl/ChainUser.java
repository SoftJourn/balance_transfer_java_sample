package com.softjourn.balance.transfer.client.chainImpl;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Set;

public class ChainUser implements User {

    private final String name;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;
    private final String mspId;

    public ChainUser(String name, File certificate, File privateKey, String mspId) {
        this.name = name;
        this.mspId = mspId;
        try {
            this.enrollment = ChainEnrolment.builder()
                    .certificate(readCertificate(certificate))
                    .privateKey(readPrivateKey(privateKey))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    @Override
    public String getAccount() {
        return this.account;
    }

    @Override
    public String getAffiliation() {
        return this.affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    @Override
    public String getMspId() {
        return this.mspId;
    }

    private String readCertificate(File file) throws IOException, CertificateException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(in, writer, StandardCharsets.UTF_8);
            return writer.toString();
        }
    }

    private PrivateKey readPrivateKey(File file) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        PEMParser parser = new PEMParser(new FileReader(file));
        PrivateKeyInfo keyInfo = PrivateKeyInfo.getInstance(parser.readObject());
        KeyFactory keyFactory = KeyFactory.getInstance(keyInfo.getPrivateKeyAlgorithm().getAlgorithm().toString());
        KeySpec privateKeySpec = new PKCS8EncodedKeySpec(keyInfo.getEncoded());
        return keyFactory.generatePrivate(privateKeySpec);
    }
}
