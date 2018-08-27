package asunder.toche.sccmanagement.custom;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import asunder.toche.sccmanagement.preference.Utils;

/**
 * Created by ToCHe on 16/8/2018 AD.
 */
public class FileEnDecryptManager {

    private String key = "abcdefg"; // 加密解密key(Encrypt or decrypt key)

    private FileEnDecryptManager() {
    }

    private static FileEnDecryptManager instance = null;

    public static FileEnDecryptManager getInstance() {
        synchronized (FileEnDecryptManager.class) {
            if (instance == null) {
                instance = new FileEnDecryptManager();
            }
        }
        return instance;
    }

    /**
     * 加密入口(encrypt intrance)
     *
     * @param fileUrl 文件绝对路径
     * @return
     */
    public boolean doEncrypt(String fileUrl) {
        //if (isDecrypted(fileUrl)) {
        if (encrypt(fileUrl)) {
            // 可在此处保存加密状态到数据库或文件(you can save state into db or file)
            System.out.println("encrypt success");
            //LogUtils.d("encrypt succeed");
            return true;
        } else {
            System.out.println("encrypt failed");
            //LogUtils.d("encrypt failed");
            return false;
        }
        /* } */
    }

    private final int REVERSE_LENGTH = 28; // 加解密长度(Encryption length)

    /**
     * 加解密(Encrypt or deprypt method)
     *
     * @param strFile 源文件绝对路径
     * @return
     */
    private boolean encrypt(String strFile) {
        int len = REVERSE_LENGTH;
        try {
            File f = new File(strFile);
            if (f.exists()) {
                RandomAccessFile raf = new RandomAccessFile(f, "rw");
                long totalLen = raf.length();

                if (totalLen < REVERSE_LENGTH)
                    len = (int) totalLen;

                FileChannel channel = raf.getChannel();
                MappedByteBuffer buffer = channel.map(
                        FileChannel.MapMode.READ_WRITE, 0, REVERSE_LENGTH);
                byte tmp;
                for (int i = 0; i < len; ++i) {
                    byte rawByte = buffer.get(i);
                    if (i <= key.length() - 1) {
                        tmp = (byte) (rawByte ^ key.charAt(i)); // 异或运算(XOR operation)
                    } else {
                        tmp = (byte) (rawByte ^ i);
                    }
                    buffer.put(i, tmp);
                }
                buffer.force();
                buffer.clear();
                channel.close();
                raf.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解密入口(decrypt intrance)
     *
     * @param fileUrl 源文件绝对路径
     */
    public void doDecrypt(String fileUrl) {
        try {
            if (!isDecrypted(fileUrl)) {
                decrypt(fileUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void decrypt(String fileUrl) {
        if (encrypt(fileUrl)) {
            // 可在此处保存解密状态到数据库或文件(you can save state into db or file)
            //("decrypt succeed");
        }
    }

    /**
     * fileName 文件是否已经解密了(Whether file has been decrypted)
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    private boolean isDecrypted(String filePath) throws IOException {
        // 从数据库或者文件中取出此路径对应的状态(get state out from db or file)
        return false;
    }

    private void yy(){
        byte[] keyBytes = new byte[0];
        try {
            keyBytes = KeyGenerator.getInstance("AES").generateKey().getEncoded();
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

    }

    public void encrypt(File file) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        // Here you read the cleartext.

        FileInputStream fis = new FileInputStream(file);
        // This stream write the encrypted text. This stream will be wrapped by another stream.
        FileOutputStream fos = new FileOutputStream(file);

        // Length is 16 byte
        // Careful when taking user input!!! https://stackoverflow.com/a/3452620/1188357
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
        // Create cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        // Wrap the output stream
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        // Write bytes
        int b;
        byte[] d = new byte[8];
        while((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        // Flush and close streams.
        cos.flush();
        cos.close();
        fis.close();
    }

    public  void decrypt(File file) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        FileInputStream fis = new FileInputStream(file);

        FileOutputStream fos = new FileOutputStream(file);
        SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[8];
        while((b = cis.read(d)) != -1) {
            fos.write(d, 0, b);
        }
        fos.flush();
        fos.close();
        cis.close();
    }

    public void fileProcessor(int cipherMode,String key,File inputFile,File outputFile){
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException e) {
            e.printStackTrace();
        }
    }
}