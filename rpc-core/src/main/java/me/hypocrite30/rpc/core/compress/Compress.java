package me.hypocrite30.rpc.core.compress;

import me.hypocrite30.rpc.common.extension.SPI;

/**
 * @Author: Hypocrite30
 * @Date: 2021/12/18 23:01
 */
@SPI
public interface Compress {
    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
