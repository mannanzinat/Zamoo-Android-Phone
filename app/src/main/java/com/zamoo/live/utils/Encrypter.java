package com.zamoo.live.utils;

import android.content.Context;
import android.content.Intent;

import com.zamoo.live.DownloadActivity;
import com.zamoo.live.service.DownloadService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encrypter {
    private final static int IV_LENGTH = 16; // Default length with Default 128
    // key AES encryption
    private final static int DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE = 1024;

    private final static String ALGO_RANDOM_NUM_GENERATOR = "SHA1PRNG";
    public final static String ALGO_SECRET_KEY_GENERATOR = "AES";
    private final static String ALGO_VIDEO_ENCRYPTOR = "AES/CBC/PKCS5Padding";


    private Context context;

    public Encrypter(Context context) {
        this.context = context;
    }

    @SuppressWarnings("resource")
    public void encrypt(SecretKey key, /*AlgorithmParameterSpec paramSpec,*/ InputStream in, OutputStream out, String inFileName,
                        int notificationId, int downloadListIndex)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException {
        try {
            byte[] iv = new byte[] { (byte) 0x8E, 0x12, 0x39, (byte) 0x9C,
                    0x07, 0x72, 0x6F, 0x5A, (byte) 0x8E, 0x12, 0x39, (byte) 0x9C,
                    0x07, 0x72, 0x6F, 0x5A };
            //generate new AlgorithmParameterSpec
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            Cipher c = Cipher.getInstance(ALGO_VIDEO_ENCRYPTOR);
            c.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            out = new CipherOutputStream(out, c);
            int count = 0;
            byte[] buffer = new byte[DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE];
            while ((count = in.read(buffer)) >= 0) {
                out.write(buffer, 0, count);
            }

            // send broadcast
            Intent intent = new Intent(DownloadService.ACTION_FINISH_DOWNLOAD);
            intent.putExtra("message", "Download Finish");
            intent.putExtra("originalFile", inFileName);
            intent.putExtra("notificationId", notificationId);
            intent.putExtra("downloadListIndex", downloadListIndex);
            context.sendBroadcast(intent);

        } finally {
            out.close();
        }
    }

    @SuppressWarnings("resource")
    public void decrypt(SecretKey key, /*AlgorithmParameterSpec paramSpec,*/ InputStream in, OutputStream out, String decryptFileName)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IOException {
        try {
            byte[] iv = new byte[] { (byte) 0x8E, 0x12, 0x39, (byte) 0x9C,
                    0x07, 0x72, 0x6F, 0x5A, (byte) 0x8E, 0x12, 0x39, (byte) 0x9C,
                    0x07, 0x72, 0x6F, 0x5A };
            // read from input stream AlgorithmParameterSpec
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
            Cipher c = Cipher.getInstance(ALGO_VIDEO_ENCRYPTOR);
            c.init(Cipher.DECRYPT_MODE, key, paramSpec);
            out = new CipherOutputStream(out, c);
            int count = 0;
            byte[] buffer = new byte[DEFAULT_READ_WRITE_BLOCK_BUFFER_SIZE];
            while ((count = in.read(buffer)) >= 0) {
                //Log.d("decrypt: ", count+"");
                out.write(buffer, 0, count);
            }

            Intent intent = new Intent(DownloadActivity.ACTION_PLAY_VIDEO);
            intent.putExtra("fileName", decryptFileName);
            context.sendBroadcast(intent);

        } finally {
            out.close();
        }
    }

    public void encryptVideo(byte[] keyData, File inFile, File outFile, int notficationId, int downloadLisIndex) {

        try {
            SecretKey key = new SecretKeySpec(keyData, 0, keyData.length, ALGO_SECRET_KEY_GENERATOR); //if you want to store key bytes to db so its just how to //recreate back key from bytes array

            encrypt(key,  new FileInputStream(inFile), new FileOutputStream(outFile), inFile.getName(), notficationId, downloadLisIndex);
            //Encrypter.decrypt(key2,  new FileInputStream(outFile), new FileOutputStream(outFile_dec));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void decryptVideo(byte[] keyData, File encryptedFile, File outFileDec) {

        try {
            SecretKey key2 = new SecretKeySpec(keyData, 0, keyData.length, ALGO_SECRET_KEY_GENERATOR); //if you want to store key bytes to db so its just how to //recreate back key from bytes array
            //Encrypter.encrypt(key,  new FileInputStream(inFile), new FileOutputStream(outFile));
            decrypt(key2,  new FileInputStream(encryptedFile), new FileOutputStream(outFileDec), outFileDec.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    // 5323-1141748-42-58-61-1274-56-22-1269548-93
}
