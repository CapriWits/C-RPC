package me.hypocrite30.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: Hypocrite30
 * @Date: 2021/11/16 21:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Entity implements Serializable {
    private String message;
    private String description;
}
