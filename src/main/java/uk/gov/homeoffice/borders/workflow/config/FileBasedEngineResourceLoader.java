package uk.gov.homeoffice.borders.workflow.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@AllArgsConstructor
public class FileBasedEngineResourceLoader implements EngineResourceLoader {

    private String location;

    @Override
    public List<ResourceContainer> getResources() {
        Collection<File> files = FileUtils.listFiles(FileUtils.getFile(location), null, true);
        return files.stream().map(f -> {
            try {
                return new ResourceContainer(new FileSystemResource(f), f.getName());
            } catch (Exception e) {
                throw new RuntimeException(String.format("Unable to create resource stream from file %s due to %s", f.getName(),
                        e.getMessage()));
            }
        }).collect(toList());

    }

    @Override
    public String storeType() {
        return "file";
    }
}
