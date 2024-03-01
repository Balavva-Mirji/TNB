package software.tnb.infinispan.resource.openshift;

import software.tnb.common.config.OpenshiftConfiguration;
import software.tnb.common.deployment.ReusableOpenshiftDeployable;
import software.tnb.common.deployment.WithName;
import software.tnb.common.openshift.OpenshiftClient;
import software.tnb.common.utils.WaitUtils;
import software.tnb.infinispan.service.Infinispan;

import org.apache.commons.io.IOUtils;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HTTPGetActionBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

@AutoService(Infinispan.class)
public class OpenshiftInfinispan extends Infinispan implements ReusableOpenshiftDeployable, WithName {

    protected static final String CONFIG_MAP_NAME = "infinispan-config";

    @Override
    public void undeploy() {
        OpenshiftClient.get().apps().deployments().withName(name()).delete();
        OpenshiftClient.get().services().withLabel(OpenshiftConfiguration.openshiftDeploymentLabel(), name()).delete();
        OpenshiftClient.get().configMaps().withName(CONFIG_MAP_NAME).delete();
        WaitUtils.waitFor(() -> servicePod() == null, "Waiting until the pod is removed");
    }

    @Override
    public void openResources() {

    }

    @Override
    public void closeResources() {

    }

    @Override
    public void create() {

        try {
            OpenshiftClient.get()
                .createConfigMap(CONFIG_MAP_NAME, Map.of("infinispan.xml", IOUtils.resourceToString("/infinispan.xml", StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Probe probe = new ProbeBuilder()
            .withHttpGet(new HTTPGetActionBuilder()
                .withPort(new IntOrString(PORT))
                .withPath("/console")
                .build()
            ).build();

        OpenshiftClient.get().apps().deployments().createOrReplace(new DeploymentBuilder()
            .withNewMetadata()
                .withName(name())
                .addToLabels(OpenshiftConfiguration.openshiftDeploymentLabel(), name())
            .endMetadata()
                .editOrNewSpec()
                    .withNewSelector()
                        .addToMatchLabels(OpenshiftConfiguration.openshiftDeploymentLabel(), name())
                    .endSelector()
                    .withReplicas(1)
                    .editOrNewTemplate()
                        .editOrNewMetadata()
                            .addToLabels(OpenshiftConfiguration.openshiftDeploymentLabel(), name())
                        .endMetadata()
                        .editOrNewSpec()
                            .addNewContainer()
                                .withName(name())
                                .withImage(defaultImage())
                                .withArgs("-c", "/user-config/infinispan.xml")
                                .addNewPort()
                                    .withContainerPort(PORT)
                                    .withName(name())
                                .endPort()
                                .addAllToEnv(containerEnvironment().entrySet().stream().map(e -> new EnvVar(e.getKey(), e.getValue(), null))
                                    .collect(Collectors.toList()))
                                .addNewVolumeMount()
                                    .withName(CONFIG_MAP_NAME)
                                    .withMountPath("/user-config")
                                .endVolumeMount()
                                .withReadinessProbe(probe)
                                .withLivenessProbe(probe)
                            .endContainer()
                            .addNewVolume()
                                .withName(CONFIG_MAP_NAME)
                                .withConfigMap(new ConfigMapVolumeSourceBuilder()
                                    .withName(CONFIG_MAP_NAME)
                                    .build())
                            .endVolume()
                        .endSpec()
                    .endTemplate()
                .endSpec()
            .build());

        OpenshiftClient.get().services().createOrReplace(new ServiceBuilder()
            .editOrNewMetadata()
            .withName(name())
            .addToLabels(OpenshiftConfiguration.openshiftDeploymentLabel(), name())
            .endMetadata()
            .editOrNewSpec()
            .addToSelector(OpenshiftConfiguration.openshiftDeploymentLabel(), name())
            .addNewPort()
            .withName(name())
            .withProtocol("TCP")
            .withPort(PORT)
            .withTargetPort(new IntOrString(PORT))
            .endPort()
            .endSpec()
            .build());
    }

    @Override
    public boolean isDeployed() {
        return OpenshiftClient.get().apps().deployments().withLabel(OpenshiftConfiguration.openshiftDeploymentLabel(), name()).list()
            .getItems().size() > 0;
    }

    @Override
    public Predicate<Pod> podSelector() {
        return WithName.super.podSelector();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public String name() {
        return "infinispan";
    }

    @Override
    public int getPortMapping() {
        return PORT;
    }

    @Override
    public String getHost() {
        return name();
    }
}
