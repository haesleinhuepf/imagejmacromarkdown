package net.haesleinhuepf.imagej.markdown;

import ij.IJ;

import java.io.File;

/**
 * The Parser goes through a macro and generates a new macro which copies the original code step by step and writes
 * a markdown file while executing it.
 *
 * @author Robert Haase
 */
public class ImagejMacroMarkdownParser {
    String code;
    StringBuilder builder;
    StringBuilder codeBuilder;

    public ImagejMacroMarkdownParser(String code, File currentFile) {
        this.code = code + "/*\n*/\n";
        ImagejMacroMarkdownRuntime.reset();
        if (currentFile == null) {
            currentFile = new File(IJ.getDirectory("temp") + "temp.ijm");
        }
        ImagejMacroMarkdownRuntime.getInstance().temporaryFolder = new File(currentFile.getParent().toString() + File.separator +
               "." + currentFile.getName() + "_ijmmdcache" + File.separator);
    }

    public String parse() {
        builder = new StringBuilder();
        codeBuilder = new StringBuilder();
        String[] lines = code.split("\n");

        boolean markdown = false;
        for (String line : lines) {
            boolean futureMarkdown = markdown;
            if (line.trim().startsWith("/*")) {
                line = line.trim();
                line = line.substring(2);
                markdown = true;
                futureMarkdown = true;
                macroToMarkdownSwitch();
            }
            if (line.trim().endsWith("*/")) {
                line = line.trim();
                line = line.substring(0, line.length() - 2);
                futureMarkdown = false;
            }

            if (markdown) {
                println(line);
            } else {
                println(line);
                exec(line);
            }

            if (markdown && !futureMarkdown) {
                markdownToMacroSwitch();
            }
            markdown = futureMarkdown;
        }
        builder.append(codeBuilder.toString());
        return builder.toString();
    }

    private void println(String line) {
        builder.append("call(\"net.haesleinhuepf.imagej.markdown.ImagejMacroMarkdownRuntime.println\", \"" + line.replace("\\", "\\\\").replace("\"", "\\\"") + "\");\n");
    }
    private void exec(String line) {
        codeBuilder.append(line + "\n");
    }

    private void macroToMarkdownSwitch() {
        builder.append(codeBuilder);
        codeBuilder = new StringBuilder();
        builder.append("call(\"net.haesleinhuepf.imagej.markdown.ImagejMacroMarkdownRuntime.macroToMarkdownSwitch\");\n");
    }

    private void markdownToMacroSwitch() {
        builder.append("call(\"net.haesleinhuepf.imagej.markdown.ImagejMacroMarkdownRuntime.markdownToMacroSwitch\");\n");
    }
}
