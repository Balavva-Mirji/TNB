package software.tnb.flink.service;

import software.tnb.common.account.NoAccount;
import software.tnb.common.client.NoClient;
import software.tnb.common.deployment.WithDockerImage;
import software.tnb.common.service.Service;
import software.tnb.common.validation.NoValidation;

public abstract class Flink extends Service<NoAccount, NoClient, NoValidation> implements WithDockerImage {

    protected static final int PORT = 8081;

    public abstract String host();

    @Override
    public String defaultImage() {
        return "quay.io/fuse_qe/flink:java17";
    }
    public int port() {
        return PORT;
    }

    public void deploy() {
    }

    public void undeploy() {
    }

    public void openResources() {
    }

    public void closeResources() {
    }
}
