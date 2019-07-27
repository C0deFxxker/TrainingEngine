package com.lyl.study.training.resource.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于Docker容器的资源数据结构
 *
 * @author liyilin
 */
@Data
@ToString
@EqualsAndHashCode
public class ContainerResource implements Resource {
    /**
     * 镜像
     */
    private String image;
    /**
     * 启动应用需要的最小CPU
     */
    private float cpu;
    /**
     * 最大CPU
     */
    private float cpuLimit;
    /**
     * 启动应用需要的最小内存
     */
    private long memoryMb;
    /**
     * 最大内存
     */
    private long memoryLimitMb;
    /**
     * Gpu资源申请，取值有以下方式：
     * <li>为0时表示不需要申请GPU资源</li>
     * <li>为整数且大于等于1时，表示选择Gpu独占模式。</li>
     * <li>为0到1之间的小数时，表示选择Gpu共享模式，该值代表占用单张卡显存的百分比。</li>
     */
    private float gpu;
    /**
     * 卷配置，Key-挂载卷，Value-挂载卷到容器中的路径。Key的取值有以下方式：
     * <li>为绝对路径时（以"/"开头），认为是本地路径文件或目录挂载到容器中</li>
     * <li>为卷名时（不以"/"开头），认为是挂载已有卷到容器中</li>
     */
    private Map<String, String> volumes = new HashMap<>();
}
