package com.lyl.study.training.resource.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于Docker容器的资源数据结构
 *
 * @author liyilin
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class K8sContainerResource extends ContainerResource {
    /**
     * 应用自身的label
     */
    private Map<String, String> labels = new HashMap<>();
    /**
     * k8s命名空间，为空时采用默认命名空间
     */
    private String namespace;
    /**
     * 容器内域名解析。Key-IP，Value-域名列表。Value内的多个域名将解析成Key指定的IP。
     */
    private Map<String, List<String>> hostAliases = new HashMap<>();
    /**
     * 节点选择器，选择拥有对应Label标记的节点部署
     */
    private Map<String, String> nodeSelector;
    /**
     * 端口映射规则
     */
    private List<PortMap> portMaps = new ArrayList<>();
    /**
     * 镜像拉取策略: [Always | Never | IfNotPresent]
     */
    private String imagePullPolicy;
    /**
     * 容器重启策略: [Always | Never | OnFailure]
     */
    private String restartPolicy;
    /**
     * 应用实例数
     */
    private int replicas;
    /**
     * K8s外挂配置(暂不支持)
     */
//    private Map<String, String> configItems = new HashMap<>();
}
