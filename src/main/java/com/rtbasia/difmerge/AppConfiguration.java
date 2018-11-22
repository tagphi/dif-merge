package com.rtbasia.difmerge;

import com.rtbasia.difmerge.ipfs.IPFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean(name = "localIpfs")
    public IPFSClient createLocalIpfs(@Value("${spring.application.ipfs.local.host}") String host,
                                      @Value("${spring.application.ipfs.local.port}") int port,
                                      @Value("${spring.application.ipfs.local.check-exists-timeout-secs}") int checkExistsTimeout,
                                      @Value("${spring.application.ipfs.local.download-timeout-secs}") int downloadTimeout) {
        return new IPFSClient(host, port, checkExistsTimeout, downloadTimeout);
    }

    @Bean(name = "remoteIpfs")
    public IPFSClient createRemoteIpfs(@Value("${spring.application.ipfs.remote.host}") String host,
                                       @Value("${spring.application.ipfs.remote.port}") int port,
                                       @Value("${spring.application.ipfs.remote.check-exists-timeout-secs}") int checkExistsTimeout,
                                       @Value("${spring.application.ipfs.remote.download-timeout-secs}") int downloadTimeout) {
        return new IPFSClient(host, port, checkExistsTimeout, downloadTimeout);
    }
}
