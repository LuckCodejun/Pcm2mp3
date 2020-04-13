package com.meetpast.pcm2mp3;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class FileUtil {

    private static final String directoryPath="/meetpast/";


    public static String getMP3FileName(long timeMillis){
        return String.format("%1$tY-%1$tm-%1$td_%1$tH_%1$tM_%1$tS_%1$tL.mp3", timeMillis);
    }

    public static String getPcmFileName(long timeMillis){
        return String.format("%1$tY-%1$tm-%1$td_%1$tH_%1$tM_%1$tS_%1$tL.pcm", timeMillis);
    }

    public static File createFile(Context context,String fileName){
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED)?Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC):context.getCacheDir();
        File folderDir = new File(rootDir.getAbsolutePath() + directoryPath);
        if (!folderDir.exists() && folderDir.mkdirs()){

        }

        return new File(folderDir, fileName);
    }


    /**
     * 大小端字节转换
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String bigtolittle( String fileName) throws IOException {

        File file = new File(fileName);    //filename为pcm文件，请自行设置

        InputStream in = null;
        byte[] bytes = null;
        in = new FileInputStream(file);
        bytes = new byte[in.available()];//in.available()是得到文件的字节数
        int length = bytes.length;
        while (length != 1) {
            long i = in.read(bytes, 0, bytes.length);
            if (i == -1) {
                break;
            }
            length -= i;
        }

        int dataLength = bytes.length;
        int shortlength = dataLength / 2;
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, dataLength);
        ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();//此处设置大小端
        short[] shorts = new short[shortlength];
        shortBuffer.get(shorts, 0, shortlength);
        File file1 = File.createTempFile("pcm", null);//输出为临时文件
        String pcmtem = file1.getPath();
        FileOutputStream fos1 = new FileOutputStream(file1);
        BufferedOutputStream bos1 = new BufferedOutputStream(fos1);
        DataOutputStream dos1 = new DataOutputStream(bos1);
        for (int i = 0; i < shorts.length; i++) {
            dos1.writeShort(shorts[i]);

        }
        dos1.close();
        Log.d("gg", "bigtolittle: " + "=" + shorts.length);
        return pcmtem;
    }
}