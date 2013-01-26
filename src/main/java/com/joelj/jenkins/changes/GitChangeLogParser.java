package com.joelj.jenkins.changes;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Parse the git log
 *
 * Copied from the default Git plugin.
 * @author Nigel Magnay
 */
public class GitChangeLogParser extends ChangeLogParser {
	private boolean authorOrCommitter;

	public GitChangeLogParser(boolean authorOrCommitter) {
		super();
		this.authorOrCommitter = authorOrCommitter;
	}

	public GitChangeSetList parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {

		Set<GitChangeSet> gitChangeSets = new LinkedHashSet<GitChangeSet>();

		// Parse the log file into GitChangeSet items - each one is a commit

		BufferedReader rdr = new BufferedReader(new FileReader(changelogFile));

		try {
			String line;
			// We use the null value to determine whether at least one commit was
			// present in the changelog. If it stays null, there is no commit line.
			List<String> lines = null;

			while ((line = rdr.readLine()) != null) {
				if (line.startsWith("commit ")) {
					if (lines != null) {
						gitChangeSets.add(parseCommit(lines, authorOrCommitter));
					}
					lines = new ArrayList<String>();
				}

				if (lines != null && lines.size() < THRESHOLD) {
					lines.add(line);
				}
			}

			if (lines != null) {
				gitChangeSets.add(parseCommit(lines, authorOrCommitter));
			}

			return new GitChangeSetList(build, new ArrayList<GitChangeSet>(gitChangeSets));
		} finally {
			rdr.close();
		}
	}

	private GitChangeSet parseCommit(List<String> lines, boolean useAuthorInsteadOfCommitter) {
		return new GitChangeSet(lines, useAuthorInsteadOfCommitter);
	}

	/**
	 * To control the memory overhead of a large change, we ignore beyond certain number of lines.
	 */
	private static final int THRESHOLD = 1000;
}
