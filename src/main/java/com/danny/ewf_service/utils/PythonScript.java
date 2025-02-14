package com.danny.ewf_service.utils;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class PythonScript {
    private final PythonInterpreter interpreter;

    public PythonScript() {
        interpreter = new PythonInterpreter();
        // Add Python scripts directory to sys.path
        interpreter.exec("import sys");
        interpreter.exec("sys.path.append('./python/scripts')");
    }

    public Object executePythonScript(String scriptName, Object... args) {
        try {
            // Load the Python script
            Resource resource = new ClassPathResource("python/scripts/" + scriptName);
            String scriptContent = new String(Files.readAllBytes(Paths.get(resource.getURI())));

            // Execute the script
            interpreter.exec(scriptContent);

            // Get the main function
            PyObject pyFunction = interpreter.get("main");

            // Convert Java arguments to Python objects and call the function
            PyObject result = pyFunction.__call__();

            // Convert Python result back to Java object
            return result.__tojava__(Object.class);

        } catch (IOException e) {
            throw new RuntimeException("Error executing Python script: " + scriptName, e);
        }
    }
}