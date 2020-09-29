package net.haesleinhuepf.imagej.markdown;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import ij.text.TextWindow;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;


/**
 * The Runtime is initialized when the macro starts being executed. After macro execution, it can deliver markdown and
 * html files which show the macro execution progress.
 *
 * @author Robert Haase
 */
public class ImagejMacroMarkdownRuntime {
    File temporaryFolder;
    private boolean temporaryFolderInitialized = false;

    StringBuilder markdownBuilder;
    StringBuilder logKeeper;
    HashMap<ImageWindow, ImageProcessor> windowsAndProcessors;
    HashMap<ResultsTable, Integer> tablesAndHashes;

    private ImagejMacroMarkdownRuntime(){
        temporaryFolder = new File(IJ.getDirectory("temp") + "ijmarkdown_" + System.currentTimeMillis() + File.separator);
        temporaryFolderInitialized = false;
        markdownBuilder = new StringBuilder();
        logKeeper = new StringBuilder();
    }

    private static ImagejMacroMarkdownRuntime instance;
    static ImagejMacroMarkdownRuntime getInstance() {
        if (instance == null) {
            instance = new ImagejMacroMarkdownRuntime();
            markdownToMacroSwitch();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public static void print(String text) {
        getInstance().markdownBuilder.append(text);
    }

    public static void println(String text) {
        getInstance().markdownBuilder.append(text + "\n");
    }


    public static void markdownToMacroSwitch() {
        IJ.log("\\Clear");

        getInstance().windowsAndProcessors = new HashMap<>();
        for (int id : WindowManager.getIDList()) {
            ImagePlus imp = WindowManager.getImage(id);
            ImageWindow window = imp.getWindow();
            ImageProcessor ip = imp.getProcessor();

            getInstance().windowsAndProcessors.put(window, ip);
        }

        getInstance().tablesAndHashes = new HashMap<>();
        for (Window window : WindowManager.getAllNonImageWindows()) {
            if (window instanceof TextWindow) {
                ResultsTable table = ((TextWindow) window).getResultsTable();
                if (table != null) {
                    String tableAsHTML = tableToHTML(table);
                    getInstance().tablesAndHashes.put(table, tableAsHTML.hashCode());
                }
            }
        }


        println("```java");
    }

    private static String tableToHTML(ResultsTable table) {
        if (table == null || table.size() == 0) {
            return "";
        }

        StringBuilder tableBuilder = new StringBuilder();
        String[] headings = table.getHeadings();
        if (headings.length == 0) {
            return "";
        }

        tableBuilder.append("<table>\n");

        tableBuilder.append("<tr>");
        for (String header : headings) {
            tableBuilder.append("<th>" + header + "</th>");
        }
        tableBuilder.append("</tr>\n");

        for (int row = 0; row < table.size(); row++) {

            tableBuilder.append("<tr>");
            for (String header : headings) {
                int column = table.getColumnIndex(header);
                tableBuilder.append("<td>" + table.getStringValue(column, row) + "</td>");
            }
            tableBuilder.append("</tr>\n");
        }

        tableBuilder.append("</table>\n");
        return tableBuilder.toString();
    }

    public static void macroToMarkdownSwitch() {

        println("```");

        String log = IJ.getLog();

        // Handle the log window
        if (log != null && log.length() > 0) {
            getInstance().logKeeper.append(log);
            println("<pre>");
            log = "> " + log.replace("\n", "\n> ");
            log = log.substring(0, log.length() - 3);
            println(log);
            println("</pre>");
        }

        // Handle recently opened images
        if (WindowManager.getImageCount() > 0) {
            ImagePlus currentImp = IJ.getImage();
            for (int id : WindowManager.getIDList()) {
                ImagePlus imp = WindowManager.getImage(id);
                ImageWindow window = imp.getWindow();
                ImageProcessor ip = imp.getProcessor();
                System.out.println(ip.hashCode());


                if (!getInstance().windowsAndProcessors.keySet().contains(window) || getInstance().windowsAndProcessors.get(window) != ip) {
                    //window.toFront();
                    //imp.show();
                    //WindowManager.setCurrentWindow(window);
                    //ImagePlus windowScreenshot = new ScreenGrabber().captureImage();
                    ImagePlus windowScreenshot = grabImage(imp);

                    long timeStamp = System.currentTimeMillis();

                    getInstance().initTemporaryFolder();
                    if (windowScreenshot.getWidth() < 250) {
                        float factor = 250 / windowScreenshot.getWidth();
                        windowScreenshot = new ImagePlus("im", windowScreenshot.getProcessor().resize((int)(windowScreenshot.getWidth() * factor), (int)(windowScreenshot.getHeight() * factor)));
                    }
                    IJ.saveAs(windowScreenshot, "png", getInstance().temporaryFolder + File.separator + "image_" + timeStamp + ".png");
                    //println("![Image](image_" + timeStamp + ".png)");
                    println("<a href=\"image_" + timeStamp + ".png\"><img src=\"image_" + timeStamp + ".png\" width=\"250\" alt=\"" + imp.getTitle() + "\"/></a>");
                }
            }

            WindowManager.setCurrentWindow(currentImp.getWindow());
        }

        // Handle recently changed tables
        for (Window window : WindowManager.getAllNonImageWindows()) {
            if (window instanceof TextWindow) {
                ResultsTable table = ((TextWindow) window).getResultsTable();
                if (table != null) {
                    String tableAsHTML = tableToHTML(table);
                    boolean writeTable = false;
                    if (!getInstance().tablesAndHashes.containsKey(table)) {
                        writeTable = true;
                    } else {
                        int hash = tableAsHTML.hashCode();

                        if (getInstance().tablesAndHashes.get(table) != hash) {
                            writeTable = true;
                        }
                    }
                    if (writeTable) {
                        println(tableAsHTML);
                    }
                }
            }
        }
    }

    private void initTemporaryFolder() {
        if (temporaryFolderInitialized) {
            return;
        }
        temporaryFolderInitialized = true;

        // Make new folder if it didn't exist yet
        getInstance().temporaryFolder.mkdirs();

        // remove files in the temp folder if I likely made them
        File[] files = temporaryFolder.listFiles();
        for (File file : files) {
            if (!file.isDirectory() && (
                    (file.getName().startsWith("image_") && file.getName().endsWith(".png")) ||
                            (file.getName().startsWith("temp_") && file.getName().endsWith(".md")) ||
                            (file.getName().startsWith("temp_") && file.getName().endsWith(".html"))
            )) {
                file.delete();
            }
        }
    }

    private static ImagePlus grabImage(ImagePlus imp) {
        Roi roi = imp.getRoi();
        imp.killRoi();
        ImagePlus duplicate = new Duplicator().run(imp);
        imp.setRoi(roi);

        duplicate.setRoi(roi);
        ImagePlus result = duplicate.flatten();

        return result;
    }

    String markdown = null;
    private void finishMarkdown() {
        markdownBuilder.append("```\n");
        markdown = markdownBuilder.toString().replace("```java\n```\n", "\n");
    }

    public static String getMarkdown() {
        getInstance().finishMarkdown();
        return getInstance().markdown;
    }

    public static String getMarkdownFilename() {
        String filename = getInstance().temporaryFolder+ File.separator  + "temp_" + System.currentTimeMillis() + ".md";
        getInstance().temporaryFolder.mkdirs();
        writeFile(filename, getMarkdown());
        return filename;
    }

    public static String getHtml() {

        Parser parser = Parser.builder().build();
        Node document = parser.parse(getMarkdown());
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);

        //html = html.replace("<pre><code>", "<pre>");
        //html = html.replace("</code></pre>", "</pre>");

        html = "<html>" + header() + "<body>\n" + html + "\n</body></html>\n";
        html = html.replace("<pre><code></code></pre>", "");
        return html;
    }

    private static String header() {
        StringBuilder header = new StringBuilder();
        header.append("<head>\n");
        header.append("<style>\n");
        header.append("@font-face{font-family:Helvetica, Arial;font-weight:400;}\n");
        header.append("body{font-family:Helvetica, Arial;}\n");
        header.append("pre{font-family:Courier New, monospace;color:#000000;padding:5px 5px;background:#eeeeee;border:1px solid #dddddd;overflow-x:auto;margin:0}\n");
        header.append("code{font-family:Courier New, monospace;}\n");
        header.append("p,ul,ol,table,pre,dl{margin:0 0 20px}\n");
        header.append("h1,h2,h3,h4,h5,h6{font-family:Helvetica, Arial;color:#222;margin:0 0 20px}\n");
        header.append("h1,h2,h3{line-height:1.1}\n");
        header.append("h1{font-size:28px}\n");
        header.append("h2{color:#393939}\n");
        header.append("h3,h4,h5,h6{color:#494949}\n");
        header.append("table{padding:5px 5px;}\n");
        header.append("th, td{font-family:Helvetica, Arial;margin:10px 10px 10px 10px; border: 1px solid #dddddd; padding: 15px;}}\n");
        header.append("</style>\n");
        header.append("</head>\n");

        return header.toString();
    }

    public static String getHtmlFilename() {
        String filename = getInstance().temporaryFolder + File.separator + "temp_" + System.currentTimeMillis() + ".html";
        getInstance().temporaryFolder.mkdirs();
        writeFile(filename, getHtml());
        return filename;
    }

    private static void writeFile(String filename, String content) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLog() {
        return getInstance().logKeeper.toString();
    }

}
