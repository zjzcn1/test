package com.zjzcn.test.workflow;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class WorkflowLoader {

    private String brickUrl;

    public Map<String, Workflow> loadWorkflows() {
        Map<String, Workflow> workflows = new LinkedHashMap<>();
        String path = WorkflowLoader.class.getResource("/workflow/").getPath();
        Collection<File> files = FileUtils.listFiles(new File(path), new String[]{"json"}, false);
        for (File file : files) {
            String json;
            try {
                json = FileUtils.readFileToString(file, "utf-8");
                log.info("workflow definition: file={} \n{}", file.getPath(), json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Workflow workflow = WorkflowBuilder.build(brickUrl, json);

            workflows.put(workflow.getCode(), workflow);
        }
        return workflows;
    }
}
