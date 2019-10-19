package uk.gov.homeoffice.borders.workflow.config;

import io.digitalpatterns.camunda.encryption.ProcessDefinitionEncryptionParser;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableDecryptor;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableEncryptionPlugin;
import io.digitalpatterns.camunda.encryption.ProcessInstanceSpinVariableEncryptor;
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
        return new ProcessInstanceSpinVariableDecryptor(passPhrase, salt);
    }

    @Bean
    public ProcessInstanceSpinVariableEncryptor processInstanceSpinVariableEncryptor() {
        return new ProcessInstanceSpinVariableEncryptor(passPhrase, salt);
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
