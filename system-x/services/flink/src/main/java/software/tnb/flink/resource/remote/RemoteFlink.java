package software.tnb.flink.resource.remote;

import software.tnb.common.deployment.RemoteService;
import software.tnb.flink.service.Flink;

import com.google.auto.service.AutoService;

@AutoService(Flink.class)
public class RemoteFlink extends Flink implements RemoteService {
    @Override
    public String host() {
        return RemoteService.super.host();
    }
}
