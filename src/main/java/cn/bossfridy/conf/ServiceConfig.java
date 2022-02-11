package cn.bossfridy.conf;

import cn.bossfridy.router.ClusterNode;
import cn.bossfridy.plugin.PluginElement;
import cn.bossfridy.utils.GsonUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "config")
public class ServiceConfig {
    @XmlElement(name = "systemName")
    private String systemName;

    @XmlElement(name = "zkAddress")
    private String zkAddress;

    @XmlElement(name = "clusterNode", type = ClusterNode.class)
    private ClusterNode clusterNode;

    @XmlElementWrapper(name = "plugins")
    @XmlElement(name = "plugin")
    private List<PluginElement> plugins;

    public String getSystemName() {
        return systemName;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public ClusterNode getClusterNode() {
        return clusterNode;
    }

    public List<PluginElement> getPluginElements() {
        return plugins;
    }

    @Override
    public String toString() {
        return GsonUtil.beanToJson(this);
    }
}

