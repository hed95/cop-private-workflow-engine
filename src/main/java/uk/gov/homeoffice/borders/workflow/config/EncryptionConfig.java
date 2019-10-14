package uk.gov.homeoffice.borders.workflow.config;

import io.digitalpatterns.camunda.encryption.ProcessDefinitionEncryptionParser;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableDecryptor;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableEncryptionPlugin;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableEncryptor;
import org.camunda.bpm.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class EncryptionConfig {

    @Value("${encryption.keys.private-path}")
    private String pathToPrivateKey;

    @Value("${encryption.keys.public-path}")
    private String pathToPublicKey;

    @Bean
    public ProcessInstanceSpinVariableDecryptor processInstanceSpinVariableDecryptor() {
        return new ProcessInstanceSpinVariableDecryptor(pathToPrivateKey);
    }

    @Bean
    public ProcessInstanceSpinVariableEncryptor processInstanceSpinVariableEncryptor() {
        return new ProcessInstanceSpinVariableEncryptor(pathToPublicKey);
    }


    @Bean
    public ProcessDefinitionEncryptionParser processDefinitionEncryptionParser(RepositoryService repositoryService) {
        return new ProcessDefinitionEncryptionParser(repositoryService);
    }

    @Bean
    public ProcessInstanceSpinVariableEncryptionPlugin plugin() {
        return new ProcessInstanceSpinVariableEncryptionPlugin(processInstanceSpinVariableEncryptor(), processInstanceSpinVariableDecryptor());
    }
}
