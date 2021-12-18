package me.hypocrite30.rpc.core.compress.gzip;

import lombok.extern.slf4j.Slf4j;
import me.hypocrite30.rpc.common.exception.RpcException;
import me.hypocrite30.rpc.core.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/18 23:02
 */
public class GzipCompress implements Compress {

    public static final int BUFFER_SIZE = 1024 * 4; // 4K

    @Override
    public byte[] compress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes buffer is null");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
        } catch (IOException e) {
            throw new RpcException("gzip compress error", e);
        }
        return out.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes buffer is null");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = gunzip.read(buffer)) > -1) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new RpcException("gzip decompress error", e);
        }
        return out.toByteArray();
    }
}
