package com.augustnagro.sbtdelegatemp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Mojo(name = "sbt")
public class SbtDelegatePlugin extends AbstractMojo {

  private static final String SBT_VERSION = "1.5.3";
  private static final String SBT_PATH_PREF = "SBT_PATH_" + SBT_VERSION;

  /**
   * SBT commands to run. 
   * For example, [clean, test, package]`
   */
  @Parameter(property = "sbtParams", required = true)
  private String[] sbtParams;

  /**
   * Parameters passed to JVM running SBT
   */
  @Parameter(property = "jvmParams", required = false)
  private String[] jvmParams;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      Preferences prefs = Preferences.userNodeForPackage(getClass());

      String cachedSbtPath = prefs.get(SBT_PATH_PREF, null);
      Path sbtPath;
      if (cachedSbtPath != null && Files.exists(Paths.get(cachedSbtPath))) {
        getLog().info("Found cached sbt at " + cachedSbtPath);
        sbtPath = Paths.get(cachedSbtPath);
      } else {
        Path tmpDir = Files.createTempDirectory(null);
        Path sbtDir = Files.createDirectory(tmpDir.resolve("sbt-" + SBT_VERSION));
        sbtPath = sbtDir.resolve("sbt/bin/sbt-launch.jar");
        prefs.put(SBT_PATH_PREF, sbtPath.toAbsolutePath().toString());
        
        /*
        Unzip sbtZip to sbtDir
        */
        getLog().info("Extracting sbt to " + sbtPath);
        try (
            InputStream is = getClass().getResourceAsStream("/sbt-" + SBT_VERSION + ".zip");
            ZipInputStream zipIn = new ZipInputStream(is)
        ) {
          ZipEntry ze;
          while ((ze = zipIn.getNextEntry()) != null) {
            Path resolvedPath = sbtDir.resolve(ze.getName()).normalize();
            if (!resolvedPath.startsWith(sbtDir)) {
              throw new RuntimeException("Illegal Path in sbt zip file: " + ze.getName());
            }
            if (ze.isDirectory()) {
              Files.createDirectories(resolvedPath);
            } else {
              Files.createDirectories(resolvedPath.getParent());
              Files.copy(zipIn, resolvedPath);
            }
          }
        }
        getLog().info("Finished extracting sbt.");
      }

      Path javaBin = Paths.get(System.getProperty("java.home") + "/bin").toAbsolutePath();
      if (System.getProperty("os.name").contains("win")) {
        javaBin = javaBin.resolve("java.exe");
      } else {
        javaBin = javaBin.resolve("java");
      }
      getLog().info("Running sbt with java from: " + javaBin);

      ArrayList<String> commands = new ArrayList<>();
      commands.add(javaBin.toString());
      for (String param : jvmParams) {
        if (!param.isEmpty()) commands.add(param);
      }
      commands.add("-jar");
      commands.add(sbtPath.toString());
      for (String param : sbtParams) {
        if (!param.isEmpty()) commands.add(param);
      }
      int sbtReturnCode = new ProcessBuilder(commands)
        .inheritIO()
        .start()
        .waitFor();

      if (sbtReturnCode != 0)
        throw new RuntimeException("SBT return code was: " + sbtReturnCode);

      getLog().info("Finished running sbt");

    } catch (Exception e) {
      throw new MojoExecutionException("Failed to execute SBT", e);
    }
  }
}
