/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2020 ImageJ developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.haesleinhuepf.imagej.markdown.adapters;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.script.ScriptException;

import ij.IJ;
import net.haesleinhuepf.imagej.markdown.ImagejMacroMarkdownParser;
import net.haesleinhuepf.imagej.markdown.ImagejMacroMarkdownRuntime;
import net.imagej.legacy.IJ1Helper;

import net.imagej.legacy.plugin.IJ1MacroEngine;
import org.scijava.ui.swing.script.TextEditor;

/**
 * A JSR-223-compliant script engine for the ImageJ 1.x macro + markdown language.
 * It uses the IJ1Macro Engine and a Markdown compiler
 *
 * @author Robert Haase
 */
public class IJ1MacroMarkdownEngine extends IJ1MacroEngine {


	public IJ1MacroMarkdownEngine(IJ1Helper ij1Helper) {
		super(ij1Helper);
	}

	@Override
	public Object eval(final String macro) throws ScriptException {
		// Determine current file
		File currentFile = null;
		for (TextEditor te : TextEditor.instances) {
			System.out.println("TE: " + te);
			if (te.isActive()) {
				currentFile = te.getEditorPane().getFile();
				System.out.println("cf: " + currentFile);
			}
		}

		// save content of the log window for later
		String formerLog = IJ.getLog();
		if (formerLog == null) {
			formerLog = "";
		}

		System.out.println("Hello markdown!");

		ImagejMacroMarkdownParser ijmmdParser = new ImagejMacroMarkdownParser(macro, currentFile);
		String parsedMacro = ijmmdParser.parse();

		System.out.println("--------------------------------------------------- macro");
		System.out.println(parsedMacro);

		Object result = super.eval(parsedMacro);

		// empty log and restore it with former and current content
		IJ.log("\\Clear");

		IJ.log(formerLog + ImagejMacroMarkdownRuntime.getLog());

		String markdown = ImagejMacroMarkdownRuntime.getMarkdown();

		System.out.println("--------------------------------------------------- markdown");
		System.out.println(markdown);

		String html = ImagejMacroMarkdownRuntime.getHtml();

		System.out.println("--------------------------------------------------- html");
		System.out.println(html);

		IJ.open(ImagejMacroMarkdownRuntime.getMarkdownFilename());

		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new File(ImagejMacroMarkdownRuntime.getHtmlFilename()).toURI());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
}
