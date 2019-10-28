package uk.gov.homeoffice.borders.workflow.config;

import io.digitalpatterns.camunda.encryption.*;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EncryptionConfig {

    @Value("${encryption.passPhrase}")
    private String passPhrase;

    @Value("${encryption.salt}")
    private String salt;

    @Bean
    public ProcessInstanceSpinVariableDecryptor processInstanceSpinVariableDecryptor() {
        return new DefaultProcessInstanceSpinVariableDecryptor(passPhrase, salt);
    }

    @Bean
    public ProcessInstanceSpinVariableEncryptor processInstanceSpinVariableEncryptor() {
        return new DefaultProcessInstanceSpinVariableEncryptor(passPhrase, salt);
    }


    @Bean
    public ProcessDefinitionEncryptionParser processDefinitionEncryptionParser(RepositoryService repositoryService) {
        return new ProcessDefinitionEncryptionParser(repositoryService);
    }

    @Bean
    public ProcessInstanceSpinVariableEncryptionPlugin plugin() {
        return new ProcessInstanceSpinVariableEncryptionPlugin(processInstanceSpinVariableEncryptor(),
                processInstanceSpinVariableDecryptor());
    }
}
