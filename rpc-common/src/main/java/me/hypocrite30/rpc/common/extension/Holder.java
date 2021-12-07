package me.hypocrite30.rpc.common.extension;

import lombok.Getter;
import lombok.Setter;

/**
 * holder which hold an object
 *
 * @Author: Hypocrite30
 * @Date: 2021/12/5 11:28
 */
@Getter
@Setter
public class Holder<T> {
    private volatile T object;
}
