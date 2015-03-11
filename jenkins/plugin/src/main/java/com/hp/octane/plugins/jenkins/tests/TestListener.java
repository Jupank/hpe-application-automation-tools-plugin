// (C) Copyright 2003-2015 Hewlett-Packard Development Company, L.P.

package com.hp.octane.plugins.jenkins.tests;

import com.hp.octane.plugins.jenkins.tests.impl.TestResultWriter;
import hudson.FilePath;
import hudson.model.AbstractBuild;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestListener {

    private static Logger logger = Logger.getLogger(TestListener.class.getName());

    public static void processBuild(AbstractBuild build) {
        FilePath resultPath = new FilePath(new FilePath(build.getRootDir()), "mqmTests.xml");
        TestResultWriter resultWriter = new TestResultWriter(resultPath, build);
        try {
            for (MqmTestsExtension ext: MqmTestsExtension.all()) {
                try {
                    if (ext.supports(build)) {
                        resultWriter.add(ext.getTestResults(build));
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error processing test results in " + ext.getClass().getName(), e);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted processing test results in " + ext.getClass().getName(), e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (XMLStreamException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            try {
                resultWriter.close();
            } catch (XMLStreamException e) {
                logger.log(Level.SEVERE, "Error processing test results", e);
            }
        }
    }
}
