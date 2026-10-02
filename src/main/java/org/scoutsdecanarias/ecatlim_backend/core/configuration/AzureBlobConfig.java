package org.scoutsdecanarias.ecatlim_backend.core.configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AzureBlobConfig {

    @Value( "${azure.blob.connection}")
    private String connectionString;

    @Bean
    public BlobServiceClient serviceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    public BlobContainerClient primaryContainer() {
        BlobContainerClient container = serviceClient().getBlobContainerClient("ecatlim-aula");
        if (container.createIfNotExists()){
            log.info("Container created {}", container.getBlobContainerName());
        }
        return container;
    }
}
